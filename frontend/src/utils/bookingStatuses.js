export const BOOKING_STATUSES = ['NEW', 'IN_REVIEW', 'ASSIGNED', 'CONFIRMED', 'REJECTED', 'CANCELLED']

export const BOOKING_STATUS_LABELS_RU = {
  NEW: 'Новая',
  IN_REVIEW: 'На рассмотрении',
  ASSIGNED: 'Назначен менеджер',
  CONFIRMED: 'Подтверждена',
  REJECTED: 'Отклонена',
  CANCELLED: 'Отменена',
}

export function bookingStatusLabel(status) {
  if (!status) return ''
  return BOOKING_STATUS_LABELS_RU[status] || status
}
