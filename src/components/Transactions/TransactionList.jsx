import React, { useState, useEffect } from 'react';
import { transactionApi } from '../../api/api';
import { format } from 'date-fns';

const TransactionList = () => {
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState({
    type: 'all',
    startDate: '',
    endDate: '',
  });

  useEffect(() => {
    fetchTransactions();
  }, []);

  const fetchTransactions = async () => {
    try {
      let response;
      if (filter.type !== 'all') {
        response = await transactionApi.getTransactionsByType(filter.type);
      } else if (filter.startDate && filter.endDate) {
        response = await transactionApi.getTransactionsByDateRange(filter.startDate, filter.endDate);
      } else {
        response = await transactionApi.getAllTransactions();
      }
      setTransactions(response.data);
    } catch (error) {
      console.error('Error fetching transactions:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    try {
      await transactionApi.deleteTransaction(id);
      setTransactions(transactions.filter(t => t.id !== id));
    } catch (error) {
      console.error('Error deleting transaction:', error);
    }
  };

  const handleFilterChange = (e) => {
    setFilter({ ...filter, [e.target.name]: e.target.value });
  };

  const applyFilters = () => {
    fetchTransactions();
  };

  if (loading) return <div className="text-center">Loading...</div>;

  return (
    <div className="p-4">
      <div className="mb-4 flex gap-4">
        <select
          name="type"
          value={filter.type}
          onChange={handleFilterChange}
          className="p-2 border rounded"
        >
          <option value="all">All Types</option>
          <option value="income">Income</option>
          <option value="expense">Expense</option>
        </select>
        <input
          type="date"
          name="startDate"
          value={filter.startDate}
          onChange={handleFilterChange}
          className="p-2 border rounded"
        />
        <input
          type="date"
          name="endDate"
          value={filter.endDate}
          onChange={handleFilterChange}
          className="p-2 border rounded"
        />
        <button
          onClick={applyFilters}
          className="bg-blue-500 text-white px-4 py-2 rounded"
        >
          Apply Filters
        </button>
      </div>

      <div className="overflow-x-auto">
        <table className="min-w-full bg-white">
          <thead>
            <tr className="bg-gray-100">
              <th className="px-6 py-3 text-left">Date</th>
              <th className="px-6 py-3 text-left">Type</th>
              <th className="px-6 py-3 text-left">Amount</th>
              <th className="px-6 py-3 text-left">Category</th>
              <th className="px-6 py-3 text-left">Account</th>
              <th className="px-6 py-3 text-left">Actions</th>
            </tr>
          </thead>
          <tbody>
            {transactions.map((transaction) => (
              <tr key={transaction.id} className="border-b">
                <td className="px-6 py-4">
                  {format(new Date(transaction.transactionDate), 'yyyy-MM-dd')}
                </td>
                <td className="px-6 py-4">{transaction.type}</td>
                <td className="px-6 py-4">
                  <span className={transaction.type === 'income' ? 'text-green-600' : 'text-red-600'}>
                    {transaction.type === 'income' ? '+' : '-'}${transaction.amount}
                  </span>
                </td>
                <td className="px-6 py-4">{transaction.category?.name}</td>
                <td className="px-6 py-4">{transaction.account}</td>
                <td className="px-6 py-4">
                  <button
                    onClick={() => handleDelete(transaction.id)}
                    className="text-red-500 hover:text-red-700"
                  >
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default TransactionList;
