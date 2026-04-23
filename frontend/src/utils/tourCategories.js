export const TOUR_CATEGORIES = ['BEACH', 'SKI', 'EXCURSION', 'CRUISE', 'CITY', 'ADVENTURE']

export const TOUR_CATEGORY_LABELS_RU = {
  BEACH: 'Пляжный отдых',
  SKI: 'Горнолыжный',
  EXCURSION: 'Экскурсионный',
  CRUISE: 'Круиз',
  CITY: 'Городской',
  ADVENTURE: 'Приключения',
}

export function tourCategoryLabel(category) {
  if (!category) return ''
  return TOUR_CATEGORY_LABELS_RU[category] || category
}
