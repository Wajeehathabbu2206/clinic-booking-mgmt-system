export const PATHS = {
  home: '/',
  login: '/login',
  register: '/register',
  doctors: '/doctors',
  doctorDetail: (id: number | string = ':id') => `/doctors/${id}`,
  dashboard: '/dashboard',
  profile: '/profile',
  clinics: '/clinics',
  appointments: '/appointments',
  calendar: '/calendar',
  medicalRecords: '/medical-records',
  notifications: '/notifications',
} as const