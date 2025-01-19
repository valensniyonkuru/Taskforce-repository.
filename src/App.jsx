import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom'
import { ToastContainer } from 'react-toastify'
import 'react-toastify/dist/ReactToastify.css'
import './App.css'

// Import components
import Dashboard from './components/Dashboard/Dashboard'
import TransactionList from './components/Transactions/TransactionList'
import TransactionForm from './components/Transactions/TransactionForm'
import BudgetList from './components/Budgets/BudgetList'
import CategoryManager from './components/Categories/CategoryManager'

function App() {
  return (
    <Router>
      <div className="min-h-screen bg-gray-100">
        {/* Navigation */}
        <nav className="bg-white shadow-lg">
          <div className="max-w-7xl mx-auto px-4">
            <div className="flex justify-between h-16">
              <div className="flex space-x-7">
                <div className="flex items-center">
                  <span className="font-semibold text-gray-500 text-lg">Wallet App</span>
                </div>
                <div className="hidden md:flex items-center space-x-1">
                  <Link to="/" className="py-4 px-2 text-gray-500 hover:text-blue-500">Dashboard</Link>
                  <Link to="/transactions" className="py-4 px-2 text-gray-500 hover:text-blue-500">Transactions</Link>
                  <Link to="/budgets" className="py-4 px-2 text-gray-500 hover:text-blue-500">Budgets</Link>
                  <Link to="/categories" className="py-4 px-2 text-gray-500 hover:text-blue-500">Categories</Link>
                </div>
              </div>
            </div>
          </div>
        </nav>

        {/* Main Content */}
        <div className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/transactions" element={
              <div className="space-y-6">
                <TransactionForm />
                <TransactionList />
              </div>
            } />
            <Route path="/budgets" element={<BudgetList />} />
            <Route path="/categories" element={<CategoryManager />} />
          </Routes>
        </div>

        {/* Toast Container for notifications */}
        <ToastContainer position="top-right" autoClose={5000} />
      </div>
    </Router>
  )
}

export default App
