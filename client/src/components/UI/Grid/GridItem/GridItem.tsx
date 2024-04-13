import classNames from "classnames"
import { ReactNode } from "react"

interface IGridItem {
  className?: string
  children: ReactNode
  isWide?: boolean
}

function GridItem({className, children, isWide = false}: IGridItem) {
  return (
	<div
	className={
	  classNames(
		className,
		"grid__item",
		{[`grid__item--wire`]: isWide}
	  )
	}
  >
	{children}
  </div>
  )
}

export default GridItem
