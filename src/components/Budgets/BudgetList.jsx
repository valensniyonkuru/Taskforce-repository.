import React, { useState, useEffect, useCallback } from 'react';
import { budgetApi } from '../../api/api';
import { toast } from 'react-toastify';

const BudgetList = () => {
  const [budgets, setBudgets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [newBudget, setNewBudget] = useState({
    category: '',
    limit: '',
    period: 'MONTHLY',
  });
  const [errors, setErrors] = useState({});

  const fetchBudgets = useCallback(async () => {
    try {
      const response = await budgetApi.getAllBudgets();
      setBudgets(response.data);
    } catch (error) {
      toast.error('Failed to fetch budgets. Please try again.');
      console.error('Error fetching budgets:', error);
    } finally {
      setLoading(false);
    }
  }, []);

  const checkExceedingBudgets = useCallback(async () => {
    try {
      const response = await budgetApi.getBudgetsExceedingLimit();
      response.data.forEach(budget => {
        toast.warning(`Budget Alert: ${budget.category} has exceeded its limit!`, {
          position: "top-right",
          autoClose: 5000,
          toastId: `budget-alert-${budget.id}`, // Prevent duplicate toasts
        });
      });
    } catch (error) {
      console.error('Error checking exceeding budgets:', error);
    }
  }, []);

  useEffect(() => {
    fetchBudgets();
    checkExceedingBudgets();
  }, [fetchBudgets, checkExceedingBudgets]);

  const validateForm = () => {
    const newErrors = {};
    if (!newBudget.category.trim()) {
      newErrors.category = 'Category is required';
    }
    if (!newBudget.limit || newBudget.limit <= 0) {
      newErrors.limit = 'Limit must be greater than 0';
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setNewBudget(prev => ({
      ...prev,
      [name]: value,
    }));
    // Clear error when user starts typing
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    setSubmitting(true);
    try {
      const response = await budgetApi.createBudget({
        ...newBudget,
        limit: parseFloat(newBudget.limit),
      });
      setBudgets(prev => [...prev, response.data]);
      setNewBudget({
        category: '',
        limit: '',
        period: 'MONTHLY',
      });
      toast.success('Budget created successfully!');
    } catch (error) {
      const errorMessage = error.response?.data?.message || 'Error creating budget';
      toast.error(errorMessage);
      console.error('Error creating budget:', error);
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this budget?')) return;
    
    try {
      await budgetApi.deleteBudget(id);
      setBudgets(prev => prev.filter(b => b.id !== id));
      toast.success('Budget deleted successfully!');
    } catch (error) {
      toast.error('Failed to delete budget. Please try again.');
      console.error('Error deleting budget:', error);
    }
  };

  if (loading) return (
    <div className="flex justify-center items-center min-h-[200px]">
      <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
    </div>
  );

  return (
    <div className="p-4">
      <form onSubmit={handleSubmit} className="mb-6 bg-white p-4 rounded-lg shadow">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700">Category</label>
            <input
              type="text"
              name="category"
              value={newBudget.category}
              onChange={handleInputChange}
              className={`mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 ${
                errors.category ? 'border-red-500' : ''
              }`}
              disabled={submitting}
              required
            />
            {errors.category && (
              <p className="mt-1 text-sm text-red-500">{errors.category}</p>
            )}
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700">Limit</label>
            <input
              type="number"
              name="limit"
              value={newBudget.limit}
              onChange={handleInputChange}
              className={`mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 ${
                errors.limit ? 'border-red-500' : ''
              }`}
              min="0"
              step="0.01"
              disabled={submitting}
              required
            />
            {errors.limit && (
              <p className="mt-1 text-sm text-red-500">{errors.limit}</p>
            )}
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700">Period</label>
            <select
              name="period"
              value={newBudget.period}
              onChange={handleInputChange}
              className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
              disabled={submitting}
            >
              <option value="MONTHLY">Monthly</option>
              <option value="WEEKLY">Weekly</option>
              <option value="YEARLY">Yearly</option>
            </select>
          </div>
        </div>
        <div className="mt-4">
          <button
            type="submit"
            disabled={submitting}
            className={`px-4 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 ${
              submitting ? 'opacity-50 cursor-not-allowed' : ''
            }`}
          >
            {submitting ? 'Creating...' : 'Create Budget'}
          </button>
        </div>
      </form>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {budgets.map((budget) => (
          <div key={budget.id} className="bg-white p-4 rounded-lg shadow">
            <div className="flex justify-between items-start">
              <div>
                <h3 className="text-lg font-semibold text-gray-900">{budget.category}</h3>
                <p className="text-sm text-gray-500">{budget.period.toLowerCase()}</p>
                <p className="mt-2 text-xl font-bold text-gray-900">
                  ${parseFloat(budget.limit).toFixed(2)}
                </p>
              </div>
              <button
                onClick={() => handleDelete(budget.id)}
                className="text-red-500 hover:text-red-700 focus:outline-none"
              >
                <svg className="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default BudgetList;
