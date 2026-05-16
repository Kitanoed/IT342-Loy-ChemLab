import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './features/auth/context/AuthContext';
import ProtectedRoute from './features/auth/components/ProtectedRoute';
import PublicRoute from './features/auth/components/PublicRoute';
import HomePage from './features/home/pages/HomePage';
import LoginPage from './features/auth/pages/LoginPage';
import RegisterPage from './features/auth/pages/RegisterPage';
import DashboardPage from './features/dashboard/pages/DashboardPage';
import InventoryPage from './features/inventory/pages/InventoryPage';
import InventoryItemPage from './features/inventory/pages/InventoryItemPage';
import InventoryFormPage from './features/inventory/pages/InventoryFormPage';
import RequestsPage from './features/requests/pages/RequestsPage';
import RequestCreatePage from './features/requests/pages/RequestCreatePage';
import RequestDetailPage from './features/requests/pages/RequestDetailPage';

function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/login" element={
        <PublicRoute><LoginPage /></PublicRoute>
      } />
      <Route path="/register" element={
        <PublicRoute><RegisterPage /></PublicRoute>
      } />
      <Route path="/dashboard" element={
        <ProtectedRoute><DashboardPage /></ProtectedRoute>
      } />
      <Route path="/inventory" element={
        <ProtectedRoute><InventoryPage /></ProtectedRoute>
      } />
      <Route path="/inventory/:itemId" element={
        <ProtectedRoute><InventoryItemPage /></ProtectedRoute>
      } />
      <Route path="/inventory/new" element={
        <ProtectedRoute><InventoryFormPage /></ProtectedRoute>
      } />
      <Route path="/inventory/:itemId/edit" element={
        <ProtectedRoute><InventoryFormPage /></ProtectedRoute>
      } />

      <Route path="/requests" element={
        <ProtectedRoute><RequestsPage /></ProtectedRoute>
      } />
      <Route path="/requests/new" element={
        <ProtectedRoute><RequestCreatePage /></ProtectedRoute>
      } />
      <Route path="/requests/:requestId" element={
        <ProtectedRoute><RequestDetailPage /></ProtectedRoute>
      } />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

function App() {
  return (
    <Router>
      <AuthProvider>
        <AppRoutes />
      </AuthProvider>
    </Router>
  );
}

export default App;
