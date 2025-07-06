import AdminSidebar from '@/components/admin/AdminSidebar';
import { Outlet } from 'react-router-dom';

export default function AdminLayout() {
  return <>
    <AdminSidebar />
    <Outlet />
  </>;
}
