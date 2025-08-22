import React, { useState } from 'react';
import { AlertCircle, CheckCircle, DollarSign, Send, User, Clock, AlertTriangle, X} from 'lucide-react';

const Dashboard = () => {
  const [formData, setFormData] = useState({
    senderEmail: '',
    receiverEmail: '',
    amount: '',
    description: '',
    type: 'TRANSFER'
  });

  const [loading, setLoading] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [transactionResult, setTransactionResult] = useState(null);
  const [error, setError] = useState('');

  const transactionTypes = [
    { value: 'TRANSFER', label: 'Transfer' },
    { value: 'PAYMENT', label: 'Payment'},
    { value: 'DEBIT', label: 'Debit' },
    { value: 'CASH_OUT', label: 'Cash Out' },
    { value: 'CASH_IN', label: 'Cash In' }
  ];

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {

      const token = localStorage.getItem('token') || sessionStorage.getItem('token');

      if (!token) {
        throw new Error('Authentication required. Please log in.')
      }

      const transactionRequest = {
        ...formData,
        amount: parseFloat(formData.amount),
        timestamp: new Date().toISOString()
      };

      const response = await fetch('http://localhost:8346/api/v1/transaction' , {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
        },
        body: JSON.stringify(transactionRequest)
      });

      if (!response.ok) {
        const errorData = await response.text();
        throw new Error(errorData || 'Transaction failed');
      }

      const result = await response.json();
      setTransactionResult(result);
      setShowModal(true);

      if (!result.isFraudSuspected) {
        setFormData({
          senderEmail: '',
          receiverEmail: '',
          amount: '',
          description: '',
          type: 'TRANSFER'
        });
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const closeModal = () => {
    setShowModal(false);
    setTransactionResult(null);
  };

  const getRiskLevel = (probability) => {
    if (probability > 0.8) return { level: 'HIGH', color: 'text-red-600', bg: 'bg-red-100' };
    if (probability >= 0.5) return { level: 'MEDIUM', color: 'text-yellow-600', bg: 'bg-yellow-100' };
    return { level: 'LOW', color: 'text-green-600', bg: 'bg-green-100' };
  };

  return (
    <div className="min-h-screen bg-gray-50 py-8 px-4 sm:px-6 lg:px-8">
      <div className="max-w-4xl mx-auto">
        {/* Header */}
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">Transaction Dashboard</h1>
          <p className="text-gray-600">Send money securely with fraud detection</p>
        </div>

        {/* Main Form */}
        <div className="bg-white rounded-xl shadow-lg p-8">
          <div className="flex items-center mb-6">
            <DollarSign className="h-6 w-6 text-blue-600 mr-3" />
            <h2 className="text-xl font-semibold text-gray-800">New Transaction</h2>
          </div>

          <div className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {/* Sender Email */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  <User className="inline h-4 w-4 mr-1" />
                  Sender Email
                </label>
                <input
                  type="email"
                  name="senderEmail"
                  value={formData.senderEmail}
                  onChange={handleInputChange}
                  required
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-colors"
                  placeholder="sender@example.com"
                />
              </div>

              {/* Receiver Email */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  <Send className="inline h-4 w-4 mr-1" />
                  Receiver Email
                </label>
                <input
                  type="email"
                  name="receiverEmail"
                  value={formData.receiverEmail}
                  onChange={handleInputChange}
                  required
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-colors"
                  placeholder="receiver@example.com"
                />
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {/* Amount */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  <DollarSign className="inline h-4 w-4 mr-1" />
                  Amount
                </label>
                <input
                  type="number"
                  name="amount"
                  value={formData.amount}
                  onChange={handleInputChange}
                  required
                  min="0"
                  step="0.01"
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-colors"
                  placeholder="0.00"
                />
              </div>

              {/* Transaction Type */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Transaction Type
                </label>
                <select
                  name="type"
                  value={formData.type}
                  onChange={handleInputChange}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-colors"
                >
                  {transactionTypes.map(type => (
                    <option key={type.value} value={type.value}>
                      {type.label}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            {/* Description */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Description (Optional)
              </label>
              <textarea
                name="description"
                value={formData.description}
                onChange={handleInputChange}
                rows={3}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-colors"
                placeholder="Payment for services, etc."
              />
            </div>

            {/* Error Message */}
            {error && (
              <div className="bg-red-50 border border-red-200 rounded-lg p-4">
                <div className="flex items-center">
                  <AlertCircle className="h-5 w-5 text-red-600 mr-2" />
                  <p className="text-red-700 text-sm">{error}</p>
                </div>
              </div>
            )}

            {/* Submit Button */}
            <button
              onClick={handleSubmit}
              disabled={loading}
              className="w-full bg-blue-600 text-white py-3 px-6 rounded-lg font-medium hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors flex items-center justify-center"
            >
              {loading ? (
                <>
                  <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white mr-2"></div>
                  Processing...
                </>
              ) : (
                <>
                  <Send className="h-5 w-5 mr-2" />
                  Send Transaction
                </>
              )}
            </button>
          </div>
        </div>

        {/* Transaction Result Modal */}
        {showModal && transactionResult && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
            <div className="bg-white rounded-xl shadow-2xl max-w-md w-full max-h-screen overflow-y-auto">
              <div className="p-6">
                {/* Modal Header */}
                <div className="flex justify-between items-center mb-6">
                  <h3 className="text-xl font-semibold text-gray-800">
                    Transaction Result
                  </h3>
                  <button
                    onClick={closeModal}
                    className="text-gray-400 hover:text-gray-600 transition-colors"
                  >
                    <X className="h-6 w-6" />
                  </button>
                </div>

                {/* Transaction Status */}
                <div className="text-center mb-6">
                  {transactionResult.isFraudSuspected ? (
                    <div className="space-y-4">
                      <div className="bg-yellow-50 rounded-full w-16 h-16 flex items-center justify-center mx-auto">
                        <AlertTriangle className="h-8 w-8 text-yellow-600" />
                      </div>
                      <div>
                        <h4 className="text-lg font-semibold text-yellow-700 mb-2">
                          Transaction Under Review
                        </h4>
                        <p className="text-yellow-600 text-sm">
                          This transaction has been flagged for manual review due to fraud detection.
                          It will be processed after verification.
                        </p>
                      </div>
                    </div>
                  ) : (
                    <div className="space-y-4">
                      <div className="bg-green-50 rounded-full w-16 h-16 flex items-center justify-center mx-auto">
                        <CheckCircle className="h-8 w-8 text-green-600" />
                      </div>
                      <div>
                        <h4 className="text-lg font-semibold text-green-700 mb-2">
                          Transaction Successful
                        </h4>
                        <p className="text-green-600 text-sm">
                          Your transaction has been processed successfully.
                        </p>
                      </div>
                    </div>
                  )}
                </div>

                {/* Transaction Details */}
                <div className="space-y-4 mb-6">
                  <div className="bg-gray-50 rounded-lg p-4 space-y-3">
                    <div className="flex justify-between">
                      <span className="text-gray-600">Transaction ID:</span>
                      <span className="font-medium">#{transactionResult.id}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-600">Amount:</span>
                      <span className="font-medium">${transactionResult.amount}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-600">From:</span>
                      <span className="font-medium text-sm">{transactionResult.senderEmail}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-600">To:</span>
                      <span className="font-medium text-sm">{transactionResult.receiverEmail}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-600">Type:</span>
                      <span className="font-medium">{transactionResult.type}</span>
                    </div>
                  </div>

                  {/* Fraud Detection Info */}
                  {transactionResult.fraudProbability !== undefined && (
                    <div className="bg-gray-50 rounded-lg p-4">
                      <h5 className="font-medium text-gray-800 mb-3">Fraud Detection Analysis</h5>
                      <div className="space-y-2">
                        <div className="flex justify-between items-center">
                          <span className="text-gray-600">Risk Level:</span>
                          <span className={`px-2 py-1 rounded-full text-xs font-medium ${getRiskLevel(transactionResult.fraudProbability).bg} ${getRiskLevel(transactionResult.fraudProbability).color}`}>
                            {getRiskLevel(transactionResult.fraudProbability).level}
                          </span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-gray-600">Fraud Score:</span>
                          <span className="font-medium">{(transactionResult.fraudProbability * 100).toFixed(1)}%</span>
                        </div>
                        {transactionResult.requiresManualReview && (
                          <div className="flex items-center text-yellow-600 text-sm mt-2">
                            <Clock className="h-4 w-4 mr-1" />
                            Requires manual review
                          </div>
                        )}
                      </div>
                    </div>
                  )}
                </div>

                {/* Close Button */}
                <button
                  onClick={closeModal}
                  className="w-full bg-gray-600 text-white py-3 px-6 rounded-lg font-medium hover:bg-gray-700 transition-colors"
                >
                  Close
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}

export default Dashboard;