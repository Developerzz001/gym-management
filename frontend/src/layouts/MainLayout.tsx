import { useState } from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { useQueryClient } from '@tanstack/react-query';
import {
  AppBar,
  Box,
  Drawer,
  IconButton,
  CircularProgress,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Toolbar,
  Typography,
  Avatar,
  Menu,
  MenuItem,
  Divider,
  Badge,
  Tooltip,
} from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import DashboardIcon from '@mui/icons-material/Dashboard';
import PeopleIcon from '@mui/icons-material/People';
import FitnessCenterIcon from '@mui/icons-material/FitnessCenter';
import RestaurantIcon from '@mui/icons-material/Restaurant';
import CardMembershipIcon from '@mui/icons-material/CardMembership';
import EventNoteIcon from '@mui/icons-material/EventNote';
import MedicationIcon from '@mui/icons-material/Medication';
import ScienceIcon from '@mui/icons-material/Science';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import LogoutIcon from '@mui/icons-material/Logout';
import NotificationsIcon from '@mui/icons-material/Notifications';
import ReceiptLongIcon from '@mui/icons-material/ReceiptLong';
import HowToRegIcon from '@mui/icons-material/HowToReg';
import BusinessIcon from '@mui/icons-material/Business';
import StoreIcon from '@mui/icons-material/Store';
import AssessmentIcon from '@mui/icons-material/Assessment';
import SettingsIcon from '@mui/icons-material/Settings';
import SwapHorizIcon from '@mui/icons-material/SwapHoriz';
import ContactPageIcon from '@mui/icons-material/ContactPage';
import { useAppDispatch, useAppSelector } from '@/app/hooks';
import { clearCredentials } from '@/features/auth/authSlice';
import { getStoredAuth, axiosClient } from '@/api/axiosClient';
import { useMarkNotificationReadMutation, useNotificationsQuery, useUnreadNotificationCountQuery } from '@/api/notificationsApi';
import { FloatingAskBot } from '@/components/chat/FloatingAskBot';
import { useMyProfileImageQuery } from '@/api/profileApi';

const drawerWidth = 260;

interface NavItem {
  label: string;
  path: string;
  icon: React.ReactNode;
}

const NAV_ITEMS: Record<string, NavItem[]> = {
  SUPER_ADMIN: [
    { label: 'Organizations', path: '/platform/organizations', icon: <BusinessIcon /> },
    { label: 'Branches', path: '/platform/branches', icon: <StoreIcon /> },
    { label: 'Organization Dashboard', path: '/platform/organization-dashboard', icon: <DashboardIcon /> },
    { label: 'Branch Dashboard', path: '/platform/branch-dashboard', icon: <TrendingUpIcon /> },
    { label: 'Reports', path: '/platform/reports', icon: <AssessmentIcon /> },
  ],
  ORGANIZATION_ADMIN: [
    { label: 'Organization Dashboard', path: '/platform/organization-dashboard', icon: <DashboardIcon /> },
    { label: 'Branches', path: '/platform/branches', icon: <StoreIcon /> },
    { label: 'Branch Managers', path: '/platform/users', icon: <PeopleIcon /> },
    { label: 'Reports', path: '/platform/reports', icon: <AssessmentIcon /> },
    { label: 'Settings', path: '/platform/settings', icon: <SettingsIcon /> },
  ],
  BRANCH_MANAGER: [
    { label: 'Branch Dashboard', path: '/platform/branch-dashboard', icon: <DashboardIcon /> },
    { label: 'Branch Staff', path: '/platform/users', icon: <PeopleIcon /> },
    { label: 'Inquiries', path: '/platform/inquiries', icon: <ContactPageIcon /> },
    { label: 'Clients', path: '/platform/clients', icon: <PeopleIcon /> },
    { label: 'Membership Plans', path: '/platform/memberships', icon: <CardMembershipIcon /> },
    { label: 'Billing & Invoices', path: '/platform/billing', icon: <ReceiptLongIcon /> },
    { label: 'Transfers', path: '/platform/transfers', icon: <SwapHorizIcon /> },
    { label: 'Settings', path: '/platform/settings', icon: <SettingsIcon /> },
  ],
  RECEPTIONIST: [
    { label: 'Inquiries', path: '/platform/inquiries', icon: <ContactPageIcon /> },
    { label: 'Clients', path: '/platform/clients', icon: <PeopleIcon /> },
    { label: 'Membership Plans', path: '/platform/memberships', icon: <CardMembershipIcon /> },
    { label: 'Billing & Invoices', path: '/platform/billing', icon: <ReceiptLongIcon /> },
  ],
  ADMIN: [
    { label: 'Dashboard', path: '/admin/dashboard', icon: <DashboardIcon /> },
    { label: 'Inquiries', path: '/admin/inquiries', icon: <ContactPageIcon /> },
    { label: 'Clients', path: '/admin/clients', icon: <PeopleIcon /> },
    { label: 'Coaches', path: '/admin/coaches', icon: <FitnessCenterIcon /> },
    { label: 'Dieticians', path: '/admin/dieticians', icon: <RestaurantIcon /> },
    { label: 'Memberships', path: '/admin/memberships', icon: <CardMembershipIcon /> },
    { label: 'Billing', path: '/admin/billing', icon: <ReceiptLongIcon /> },
    { label: 'Attendance', path: '/admin/attendance', icon: <HowToRegIcon /> },
  ],
  FITNESS_COACH: [
    { label: 'Dashboard', path: '/coach/dashboard', icon: <DashboardIcon /> },
    { label: 'Assigned Clients', path: '/coach/clients', icon: <PeopleIcon /> },
    { label: 'Exercises', path: '/coach/exercises', icon: <FitnessCenterIcon /> },
    { label: 'Workout Plans', path: '/coach/workout-plans', icon: <FitnessCenterIcon /> },
    { label: 'Sessions', path: '/coach/sessions', icon: <EventNoteIcon /> },
  ],
  COACH: [
    { label: 'Dashboard', path: '/coach/dashboard', icon: <DashboardIcon /> },
    { label: 'Assigned Clients', path: '/coach/clients', icon: <PeopleIcon /> },
    { label: 'Exercises', path: '/coach/exercises', icon: <FitnessCenterIcon /> },
    { label: 'Workout Plans', path: '/coach/workout-plans', icon: <FitnessCenterIcon /> },
    { label: 'Sessions', path: '/coach/sessions', icon: <EventNoteIcon /> },
  ],
  DIETICIAN: [
    { label: 'Dashboard', path: '/dietician/dashboard', icon: <DashboardIcon /> },
    { label: 'Diet Plans', path: '/dietician/diet-plans', icon: <RestaurantIcon /> },
    { label: 'Supplements', path: '/dietician/supplements', icon: <ScienceIcon /> },
    { label: 'Medicines', path: '/dietician/medicines', icon: <MedicationIcon /> },
  ],
  CLIENT: [
    { label: 'Dashboard', path: '/client/dashboard', icon: <DashboardIcon /> },
    { label: 'My Workout', path: '/client/workout', icon: <FitnessCenterIcon /> },
    { label: 'My Diet', path: '/client/diet', icon: <RestaurantIcon /> },
    { label: 'My Progress', path: '/client/progress', icon: <TrendingUpIcon /> },
    { label: 'My Invoices', path: '/client/invoices', icon: <ReceiptLongIcon /> },
    { label: 'Notifications', path: '/client/notifications', icon: <NotificationsIcon /> },
    { label: 'My Profile', path: '/profile', icon: <PeopleIcon /> },
  ],
};

export function MainLayout() {
  const [mobileOpen, setMobileOpen] = useState(false);
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [notificationsAnchorEl, setNotificationsAnchorEl] = useState<null | HTMLElement>(null);
  const dispatch = useAppDispatch();
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const location = useLocation();
  const { firstName, lastName, role } = useAppSelector((state) => state.auth);
  const displayFirstName = role === 'SUPER_ADMIN' ? 'Super' : firstName;
  const canLoadProfileImage = !!role;
  const { data: profileImage } = useMyProfileImageQuery(canLoadProfileImage);
  const profileImageUrl = profileImage;
  const { data: unreadCount } = useUnreadNotificationCountQuery();
  const { data: notificationsPage, isLoading: notificationsLoading } = useNotificationsQuery(0, 10);
  const markAsReadMutation = useMarkNotificationReadMutation();

  const navItems = (role && NAV_ITEMS[role]) || [];
  const notificationsOpen = Boolean(notificationsAnchorEl);


  const handleLogout = async () => {
    const auth = getStoredAuth();
    await queryClient.cancelQueries();
    queryClient.removeQueries();
    dispatch(clearCredentials());
    try {
      if (auth?.refreshToken) {
        await axiosClient.post('/auth/logout', { refreshToken: auth.refreshToken });
      }
    } catch {
      // ignore logout errors, clear client state regardless
    }
    navigate('/login');
  };

  const handleNotificationClick = (event: React.MouseEvent<HTMLElement>) => {
    setNotificationsAnchorEl(event.currentTarget);
  };

  const handleNotificationsClose = () => {
    setNotificationsAnchorEl(null);
  };

  const handleNotificationItemClick = (id: number, isRead: boolean) => {
    if (!isRead && !markAsReadMutation.isPending) {
      markAsReadMutation.mutate(id);
    }
  };

  const drawer = (
    <Box>
      <Toolbar>
        <Typography variant="h6" noWrap fontWeight={700} color="primary">
          🏋️ Stryker Fit
        </Typography>
      </Toolbar>
      <Divider />
      <List>
        {navItems.map((item) => (
          <ListItemButton
            key={item.path}
            selected={location.pathname === item.path}
            onClick={() => {
              navigate(item.path);
              setMobileOpen(false);
            }}
          >
            <ListItemIcon>{item.icon}</ListItemIcon>
            <ListItemText primary={item.label} />
          </ListItemButton>
        ))}
      </List>
    </Box>
  );

  return (
    <Box sx={{ display: 'flex' }}>
      <AppBar
        position="fixed"
        color="inherit"
        sx={{ width: { sm: `calc(100% - ${drawerWidth}px)` }, ml: { sm: `${drawerWidth}px` } }}
      >
        <Toolbar sx={{ display: 'flex', justifyContent: 'space-between' }}>
          <IconButton
            color="inherit"
            edge="start"
            onClick={() => setMobileOpen(!mobileOpen)}
            sx={{ mr: 2, display: { sm: 'none' } }}
          >
            <MenuIcon />
          </IconButton>
          <Typography variant="h6" noWrap component="div" sx={{ flexGrow: 1 }}>
            {role === 'SUPER_ADMIN' ? 'Super Administration' : role === 'FITNESS_COACH' || role === 'COACH' ? 'Fitness Coach Portal' : role === 'DIETICIAN' ? 'Dietician Portal' : role === 'CLIENT' ? 'Client Portal' : role === 'BRANCH_MANAGER' || role === 'RECEPTIONIST' ? 'Branch Operations' : 'Organization Administration'}
          </Typography>
          <Tooltip title="Notifications">
            <IconButton color="inherit" onClick={handleNotificationClick}>
              <Badge badgeContent={unreadCount ?? 0} color="error">
                <NotificationsIcon />
              </Badge>
            </IconButton>
          </Tooltip>
          <Menu
            anchorEl={notificationsAnchorEl}
            open={notificationsOpen}
            onClose={handleNotificationsClose}
            PaperProps={{ sx: { width: 360, maxWidth: '92vw' } }}
          >
            <Box sx={{ px: 2, py: 1.5 }}>
              <Typography variant="subtitle1" fontWeight={700}>Notifications</Typography>
              <Typography variant="caption" color="text.secondary">
                Unread: {unreadCount ?? 0}
              </Typography>
            </Box>
            <Divider />
            {notificationsLoading ? (
              <Box sx={{ py: 3, display: 'flex', justifyContent: 'center' }}>
                <CircularProgress size={22} />
              </Box>
            ) : notificationsPage?.content?.length ? (
              <List sx={{ py: 0 }}>
                {notificationsPage.content.map((notification) => (
                  <ListItem key={notification.id} disablePadding>
                    <ListItemButton onClick={() => handleNotificationItemClick(notification.id, notification.isRead)}>
                      <ListItemText
                        primary={notification.message}
                        secondary={new Date(notification.createdAt).toLocaleString()}
                        primaryTypographyProps={{
                          variant: 'body2',
                          fontWeight: notification.isRead ? 400 : 700,
                        }}
                      />
                    </ListItemButton>
                  </ListItem>
                ))}
              </List>
            ) : (
              <Box sx={{ px: 2, py: 2.5 }}>
                <Typography variant="body2" color="text.secondary">
                  No notifications yet.
                </Typography>
              </Box>
            )}
          </Menu>
          <IconButton onClick={(e) => setAnchorEl(e.currentTarget)} sx={{ ml: 1 }}>
            <Avatar src={profileImageUrl} sx={{ width: 34, height: 34, bgcolor: 'primary.main' }}>
              {displayFirstName?.[0]}
              {lastName?.[0]}
            </Avatar>
          </IconButton>
          <Menu anchorEl={anchorEl} open={!!anchorEl} onClose={() => setAnchorEl(null)}>
            <MenuItem disabled>{displayFirstName} {lastName}</MenuItem>
            <MenuItem onClick={() => { setAnchorEl(null); navigate('/profile'); }}>My Profile</MenuItem>
            <Divider />
            <MenuItem onClick={handleLogout}>
              <LogoutIcon fontSize="small" sx={{ mr: 1 }} /> Logout
            </MenuItem>
          </Menu>
        </Toolbar>
      </AppBar>
      <Box component="nav" sx={{ width: { sm: drawerWidth }, flexShrink: { sm: 0 } }}>
        <Drawer
          variant="temporary"
          open={mobileOpen}
          onClose={() => setMobileOpen(false)}
          ModalProps={{ keepMounted: true }}
          sx={{ display: { xs: 'block', sm: 'none' }, '& .MuiDrawer-paper': { width: drawerWidth } }}
        >
          {drawer}
        </Drawer>
        <Drawer
          variant="permanent"
          sx={{ display: { xs: 'none', sm: 'block' }, '& .MuiDrawer-paper': { width: drawerWidth, boxSizing: 'border-box' } }}
          open
        >
          {drawer}
        </Drawer>
      </Box>
      <Box component="main" sx={{ flexGrow: 1, p: 3, width: { sm: `calc(100% - ${drawerWidth}px)` } }}>
        <Toolbar />
        <Outlet />
      </Box>
      <FloatingAskBot />
    </Box>
  );
}
