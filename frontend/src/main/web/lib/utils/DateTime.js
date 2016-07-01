import countdown from 'countdown'
import invariant from 'invariant'

export function datePropValidator (props, propName, componentName) {
  const value = props[propName]
  const date = Date.parse(value)
  if (!date) {
    return new Error(`Invalid prop [${propName}] supplied to ${componentName}. It's value needs to be a valid date`)
  }
}

export function duration(from, to) {
  if (from && to) {
    const fromDate = Date.parse(from)
    const toDate = Date.parse(to)
    invariant(fromDate, 'from date is not a valid date format. See ISO javascript date format.')
    invariant(toDate, 'to date is not a valid date format. See ISO javascript date format.')
    const duration = countdown(fromDate, toDate)
    return duration.toString() || 'less than 1 second'
  }
  return null
}

const padSingleDigit = (num) => {
  return num < 10 ? `0${num}` : num
}

// if we want a more dedicated date formatting library, we could use moment.
// But it brings along a big file with all the locales in it.
// for now this will do the job.
export function formatDate(dateStr) {
  if (dateStr && Date.parse(dateStr)) {
    const date = new Date(dateStr)
    const month = padSingleDigit(date.getMonth() + 1)
    const dayOfMonth = padSingleDigit(date.getDate())
    const hours = padSingleDigit(date.getHours())
    const minutes = padSingleDigit(date.getMinutes())
    const seconds = padSingleDigit(date.getSeconds())

    return `${date.getFullYear()}-${month}-${dayOfMonth} ${hours}:${minutes}:${seconds}`
  }
  return ''
}

