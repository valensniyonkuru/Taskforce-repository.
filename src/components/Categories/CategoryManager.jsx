import React, { useState, useEffect } from 'react';
import { categoryApi } from '../../api/api';

const CategoryManager = () => {
  const [categories, setCategories] = useState([]);
  const [rootCategories, setRootCategories] = useState([]);
  const [newCategory, setNewCategory] = useState({
    name: '',
    parentCategory: null,
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  useEffect(() => {
    fetchCategories();
  }, []);

  const fetchCategories = async () => {
    try {
      const [allCategoriesResponse, rootCategoriesResponse] = await Promise.all([
        categoryApi.getAllCategories(),
        categoryApi.getRootCategories()
      ]);
      setCategories(allCategoriesResponse.data);
      setRootCategories(rootCategoriesResponse.data);
      setError(null);
    } catch (error) {
      console.error('Error fetching categories:', error);
      setError(error.message || 'Failed to fetch categories. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    if (name === 'parentCategory') {
      setNewCategory({
        ...newCategory,
        parentCategory: value ? { id: parseInt(value) } : null
      });
    } else {
      setNewCategory({
        ...newCategory,
        [name]: value
      });
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);
    try {
      await categoryApi.createCategory(newCategory);
      await fetchCategories();
      setNewCategory({
        name: '',
        parentCategory: null
      });
      setSuccess('Category created successfully!');
    } catch (error) {
      console.error('Error creating category:', error);
      setError(error.message || 'Failed to create category. Please try again.');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this category? Subcategories will be moved to the parent category.')) {
      return;
    }
    setError(null);
    setSuccess(null);
    try {
      await categoryApi.deleteCategory(id);
      await fetchCategories();
      setSuccess('Category deleted successfully!');
    } catch (error) {
      console.error('Error deleting category:', error);
      setError(error.message || 'Failed to delete category. Please try again.');
    }
  };

  const renderCategoryTree = (categories) => {
    if (!categories || categories.length === 0) return null;

    return (
      <ul className="pl-4">
        {categories.map((category) => (
          <li key={category.id} className="mb-2">
            <div className="flex items-center justify-between bg-white p-2 rounded-lg shadow-sm">
              <span className="font-medium">{category.name}</span>
              <button
                onClick={() => handleDelete(category.id)}
                className="text-red-600 hover:text-red-800 text-sm"
              >
                Delete
              </button>
            </div>
            {renderCategoryTree(category.subCategories)}
          </li>
        ))}
      </ul>
    );
  };

  if (loading) return <div className="text-center">Loading...</div>;

  return (
    <div className="p-4">
      {error && (
        <div className="mb-4 p-4 bg-red-100 text-red-700 rounded-lg">
          {error}
        </div>
      )}
      
      {success && (
        <div className="mb-4 p-4 bg-green-100 text-green-700 rounded-lg">
          {success}
        </div>
      )}
      
      <form onSubmit={handleSubmit} className="mb-6 bg-white p-4 rounded-lg shadow">
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700">Category Name</label>
            <input
              type="text"
              name="name"
              value={newCategory.name}
              onChange={handleInputChange}
              className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
              required
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700">Parent Category (Optional)</label>
            <select
              name="parentCategory"
              value={newCategory.parentCategory ? newCategory.parentCategory.id : ''}
              onChange={handleInputChange}
              className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
            >
              <option value="">None</option>
              {categories.map((category) => (
                <option key={category.id} value={category.id}>
                  {category.name}
                </option>
              ))}
            </select>
          </div>
        </div>
        <div className="mt-4 flex justify-end">
          <button
            type="submit"
            className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            Add Category
          </button>
        </div>
      </form>

      <div className="bg-gray-50 p-4 rounded-lg">
        <h2 className="text-lg font-semibold mb-4">Category Hierarchy</h2>
        {renderCategoryTree(rootCategories)}
      </div>
    </div>
  );
};

export default CategoryManager;
