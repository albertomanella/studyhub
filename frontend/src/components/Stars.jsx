export default function Stars({ value, onRate, disabled, size = 'normal' }) {
  const cls = size === 'sm' ? 'star star-sm' : 'star'
  return (
    <div className="stars">
      {[1, 2, 3, 4, 5].map(n => (
        <button
          key={n}
          type="button"
          className={`${cls}${n <= value ? ' active' : ''}${disabled ? ' disabled' : ''}`}
          onClick={() => !disabled && onRate && onRate(n)}
          title={disabled ? '' : `${n} stelle`}
        >
          ★
        </button>
      ))}
    </div>
  )
}
