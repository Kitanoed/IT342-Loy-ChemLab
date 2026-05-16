import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Homepage from './pages/Homepage';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import Inventory from './pages/Inventory';
import InventoryItem from './pages/InventoryItem';
import InventoryForm from './pages/InventoryForm';
import RequestsPage from './features/requests/pages/RequestsPage';
import RequestCreatePage from './features/requests/pages/RequestCreatePage';
import RequestDetailPage from './features/requests/pages/RequestDetailPage';

function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<Homepage />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/dashboard" element={
        <ProtectedRoute><Dashboard /></ProtectedRoute>
      } />
      <Route path="/inventory" element={
        <ProtectedRoute><Inventory /></ProtectedRoute>
      } />
      <Route path="/inventory/new" element={
        <ProtectedRoute><InventoryForm /></ProtectedRoute>
      } />
      <Route path="/inventory/:itemId/edit" element={
        <ProtectedRoute><InventoryForm /></ProtectedRoute>
      } />
      <Route path="/inventory/:itemId" element={
        <ProtectedRoute><InventoryItem /></ProtectedRoute>
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
