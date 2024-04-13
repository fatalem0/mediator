import classNames from "classnames"
import { ReactNode } from "react"
import "./Grid.pcss"

interface IGrid {
  className?: string
  columns?: number
  children: ReactNode
}

function Grid({className, columns = 1, children}: IGrid) {
  return (
	<div
	  className={
		classNames(
		  className,
		  "grid",
		  {[`grid--${columns}`]: columns > 1}
		)
	  }
	>
	  {children}
	</div>
  )
}

export default Grid
