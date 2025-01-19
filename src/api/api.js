import axios from 'axios';

const API_BASE_URL = 'http://localhost:8082/api';

// Create axios instance with default config
const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000, // 10 seconds
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor
api.interceptors.request.use(
  (config) => {
    // You can add auth token here if needed
    // const token = localStorage.getItem('token');
    // if (token) {
    //   config.headers.Authorization = `Bearer ${token}`;
    // }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const { response } = error;
    if (response?.data?.error) {
      error.message = response.data.error;
    } else if (response) {
      // Handle specific HTTP errors
      switch (response.status) {
        case 400:
          error.message = 'Bad Request';
          break;
        case 401:
          error.message = 'Unauthorized';
          // Handle authentication error (e.g., redirect to login)
          break;
        case 403:
          error.message = 'Forbidden';
          break;
        case 404:
          error.message = 'Not Found';
          break;
        case 500:
          error.message = 'Internal Server Error';
          break;
        default:
          error.message = 'Something went wrong';
      }
    } else if (error.request) {
      // Network error
      error.message = 'Network Error. Please check your connection.';
    }
    return Promise.reject(error);
  }
);

// Transaction API calls
export const transactionApi = {
  getAllTransactions: () => api.get('/transactions'),
  getTransactionById: (id) => api.get(`/transactions/${id}`),
  getTransactionsByType: (type) => api.get(`/transactions/type/${type}`),
  getTransactionsByCategory: (categoryId) => api.get(`/transactions/category/${categoryId}`),
  getTransactionsByDateRange: (startDate, endDate) => 
    api.get('/transactions/date-range', { 
      params: { startDate, endDate }
    }),
  createTransaction: (transaction) => api.post('/transactions', transaction),
  updateTransaction: (id, transaction) => api.put(`/transactions/${id}`, transaction),
  deleteTransaction: (id) => api.delete(`/transactions/${id}`),
};

// Category API calls
export const categoryApi = {
  getAllCategories: () => api.get('/categories'),
  getRootCategories: () => api.get('/categories/root'),
  getSubcategories: (id) => api.get(`/categories/${id}/subcategories`),
  getCategoryByName: (name) => api.get(`/categories/name/${name}`),
  getCategoryById: (id) => api.get(`/categories/${id}`),
  createCategory: (category) => api.post('/categories', category),
  updateCategory: (id, category) => api.put(`/categories/${id}`, category),
  deleteCategory: (id) => api.delete(`/categories/${id}`),
};

// Budget API calls
export const budgetApi = {
  getAllBudgets: () => api.get('/budgets'),
  getActiveBudgets: () => api.get('/budgets/active'),
  getBudgetById: (id) => api.get(`/budgets/${id}`),
  getBudgetsExceedingLimit: () => api.get('/budgets/exceeding'),
  createBudget: (budget) => api.post('/budgets', budget),
  updateBudget: (id, budget) => api.put(`/budgets/${id}`, budget),
  deleteBudget: (id) => api.delete(`/budgets/${id}`),
  updateBudgetSpending: (id) => api.post(`/budgets/${id}/update-spending`),
};
