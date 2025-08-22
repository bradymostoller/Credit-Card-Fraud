from flask import Flask, request, jsonify
import joblib
import pandas as pd
import numpy as np
from flask_cors import CORS
import logging

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(__name__)
CORS(app)  # Enable CORS for cross-origin requests

# Load the trained model
try:
    model = joblib.load("fraud_detection_pipeline.pkl")
    logger.info("Model loaded successfully")
except Exception as e:
    logger.error(f"Error loading model: {e}")
    model = None

@app.route('/health', methods=['GET'])
def health_check():
    """Health check endpoint"""
    return jsonify({"status": "healthy", "model_loaded": model is not None})

@app.route('/predict', methods=['POST'])
def predict_fraud():
    try:
        if model is None:
            return jsonify({"error": "Model not loaded"}), 500
        
        data = request.get_json()
        if not data:
            return jsonify({"error": "No JSON data provided"}), 400
        
        required_fields = ['type', 'amount', 'oldbalanceOrg', 'newbalanceOrig', 'oldbalanceDest', 'newbalanceDest']
        missing_fields = [field for field in required_fields if field not in data]
        if missing_fields:
            return jsonify({"error": f"Missing required fields: {missing_fields}"}), 400
        
        # Create DataFrame from request
        df = pd.DataFrame([{
            'type': data['type'],
            'amount': float(data['amount']),
            'oldbalanceOrg': float(data['oldbalanceOrg']),
            'newbalanceOrig': float(data['newbalanceOrig']),
            'oldbalanceDest': float(data['oldbalanceDest']),
            'newbalanceDest': float(data['newbalanceDest'])
        }])
        
        # Compute new features (frac_sent, frac_received)
        df['frac_sent'] = df['amount'] / (df['oldbalanceOrg'] + 1e-6)
        df['frac_received'] = df['amount'] / (df['oldbalanceDest'] + 1e-6)
        
        # Make prediction
        prediction = model.predict(df)[0]
        probability = model.predict_proba(df)[0]
        fraud_probability = float(probability[1])
        
        result = {
            "is_fraud": bool(prediction),
            "fraud_probability": fraud_probability,
            "confidence": "high" if fraud_probability > 0.8 or fraud_probability < 0.2 else "medium"
        }
        
        logger.info(f"Prediction made: {result}")
        return jsonify(result)
        
    except ValueError as e:
        logger.error(f"Value error: {e}")
        return jsonify({"error": f"Invalid data format: {str(e)}"}), 400
    except Exception as e:
        logger.error(f"Prediction error: {e}")
        return jsonify({"error": f"Prediction failed: {str(e)}"}), 500



@app.route('/predict/batch', methods=['POST'])
def predict_fraud_batch():
    try:
        if model is None:
            return jsonify({"error": "Model not loaded"}), 500

        data = request.get_json()
        if not data or 'transactions' not in data:
            return jsonify({"error": "No transactions provided"}), 400

        transactions = data['transactions']
        results = []
        THRESHOLD = 0.8  # Only classify as fraud if probability > 80%

        for i, transaction in enumerate(transactions):
            try:
                required_fields = [
                    'type', 'amount', 'oldbalanceOrg', 'newbalanceOrig',
                    'oldbalanceDest', 'newbalanceDest'
                ]
                missing_fields = [f for f in required_fields if f not in transaction]
                if missing_fields:
                    results.append({
                        "index": i,
                        "error": f"Missing required fields: {missing_fields}"
                    })
                    continue

                df = pd.DataFrame([{
                    'type': transaction['type'],
                    'amount': float(transaction['amount']),
                    'oldbalanceOrg': float(transaction['oldbalanceOrg']),
                    'newbalanceOrig': float(transaction['newbalanceOrig']),
                    'oldbalanceDest': float(transaction['oldbalanceDest']),
                    'newbalanceDest': float(transaction['newbalanceDest'])
                }])

                prob = model.predict_proba(df)[0]
                fraud_probability = float(prob[1])
                is_fraud = fraud_probability > THRESHOLD

                results.append({
                    "index": i,
                    "is_fraud": bool(is_fraud),
                    "fraud_probability": fraud_probability,
                    "confidence": "high" if fraud_probability > 0.8 or fraud_probability < 0.2 else "medium"
                })

            except Exception as e:
                results.append({
                    "index": i,
                    "error": str(e)
                })

        return jsonify({"results": results})

    except Exception as e:
        logger.error(f"Batch prediction error: {e}")
        return jsonify({"error": f"Batch prediction failed: {str(e)}"}), 500


if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5001)