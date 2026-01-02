import { Navigate, createBrowserRouter } from 'react-router-dom';
import { AppLayout } from './components/layout/AppLayout';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { OrganizationsPage } from './pages/OrganizationsPage';
import { ProjectsPage } from './pages/ProjectsPage';
import { BoardPage } from './pages/BoardPage';
import { ProtectedRoute } from './components/layout/ProtectedRoute';

export const router = createBrowserRouter([
  {
    path: '/',
    element: <AppLayout />,
    children: [
      { index: true, element: <Navigate to="/organizations" replace /> },
      { path: 'login', element: <LoginPage /> },
      { path: 'register', element: <RegisterPage /> },
      {
        element: <ProtectedRoute />,
        children: [
          { path: 'organizations', element: <OrganizationsPage /> },
          { path: 'organizations/:organizationId/projects', element: <ProjectsPage /> },
          { path: 'projects/:projectId/board', element: <BoardPage /> }
        ]
      }
    ]
  }
]);
