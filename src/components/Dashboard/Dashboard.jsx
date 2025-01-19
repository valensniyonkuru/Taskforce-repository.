import React, { useState, useEffect } from 'react';
import { transactionApi } from '../../api/api';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  Title,
  Tooltip,
  Legend,
  ArcElement,
} from 'chart.js';
import { Line, Bar, Pie } from 'react-chartjs-2';

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  Title,
  Tooltip,
  Legend,
  ArcElement
);

const Dashboard = () => {
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [timeRange, setTimeRange] = useState('month');

  useEffect(() => {
    fetchTransactions();
  }, [timeRange]);

  const fetchTransactions = async () => {
    try {
      const endDate = new Date();
      const startDate = new Date();
      
      if (timeRange === 'month') {
        startDate.setMonth(startDate.getMonth() - 1);
      } else if (timeRange === 'week') {
        startDate.setDate(startDate.getDate() - 7);
      } else if (timeRange === 'year') {
        startDate.setFullYear(startDate.getFullYear() - 1);
      }

      const response = await transactionApi.getTransactionsByDateRange(
        startDate.toISOString().split('T')[0],
        endDate.toISOString().split('T')[0]
      );
      setTransactions(response.data);
    } catch (error) {
      console.error('Error fetching transactions:', error);
    } finally {
      setLoading(false);
    }
  };

  const calculateSummary = () => {
    const summary = {
      totalIncome: 0,
      totalExpense: 0,
      balance: 0,
      byCategory: {},
      byAccount: {},
    };

    transactions.forEach((transaction) => {
      if (transaction.type === 'income') {
        summary.totalIncome += transaction.amount;
      } else {
        summary.totalExpense += transaction.amount;
      }

      // Category summary
      const category = transaction.category?.name || 'Uncategorized';
      if (!summary.byCategory[category]) {
        summary.byCategory[category] = 0;
      }
      summary.byCategory[category] += transaction.amount;

      // Account summary
      if (!summary.byAccount[transaction.account]) {
        summary.byAccount[transaction.account] = 0;
      }
      summary.byAccount[transaction.account] += transaction.type === 'income' ? transaction.amount : -transaction.amount;
    });

    summary.balance = summary.totalIncome - summary.totalExpense;
    return summary;
  };

  const prepareChartData = (summary) => {
    const categoryData = {
      labels: Object.keys(summary.byCategory),
      datasets: [
        {
          data: Object.values(summary.byCategory),
          backgroundColor: [
            '#FF6384',
            '#36A2EB',
            '#FFCE56',
            '#4BC0C0',
            '#9966FF',
            '#FF9F40',
          ],
        },
      ],
    };

    const accountData = {
      labels: Object.keys(summary.byAccount),
      datasets: [
        {
          label: 'Balance by Account',
          data: Object.values(summary.byAccount),
          backgroundColor: 'rgba(54, 162, 235, 0.5)',
        },
      ],
    };

    return { categoryData, accountData };
  };

  if (loading) return <div className="text-center">Loading...</div>;

  const summary = calculateSummary();
  const { categoryData, accountData } = prepareChartData(summary);

  return (
    <div className="p-4">
      {/* Time Range Selector */}
      <div className="mb-6">
        <select
          value={timeRange}
          onChange={(e) => setTimeRange(e.target.value)}
          className="p-2 border rounded"
        >
          <option value="week">Last Week</option>
          <option value="month">Last Month</option>
          <option value="year">Last Year</option>
        </select>
      </div>

      {/* Summary Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
        <div className="bg-white p-4 rounded-lg shadow">
          <h3 className="text-lg font-semibold text-green-600">Total Income</h3>
          <p className="text-2xl">${summary.totalIncome.toFixed(2)}</p>
        </div>
        <div className="bg-white p-4 rounded-lg shadow">
          <h3 className="text-lg font-semibold text-red-600">Total Expenses</h3>
          <p className="text-2xl">${summary.totalExpense.toFixed(2)}</p>
        </div>
        <div className="bg-white p-4 rounded-lg shadow">
          <h3 className="text-lg font-semibold text-blue-600">Balance</h3>
          <p className="text-2xl">${summary.balance.toFixed(2)}</p>
        </div>
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="bg-white p-4 rounded-lg shadow">
          <h3 className="text-lg font-semibold mb-4">Expenses by Category</h3>
          <div className="h-64">
            <Pie data={categoryData} options={{ maintainAspectRatio: false }} />
          </div>
        </div>
        <div className="bg-white p-4 rounded-lg shadow">
          <h3 className="text-lg font-semibold mb-4">Balance by Account</h3>
          <div className="h-64">
            <Bar
              data={accountData}
              options={{
                maintainAspectRatio: false,
                scales: {
                  y: {
                    beginAtZero: true,
                  },
                },
              }}
            />
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
