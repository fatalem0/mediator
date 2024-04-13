import classNames from "classnames"
import { useMemo } from "react"
import SvgIcon from "../SvgIcon/SvgIcon"
import "./Button.pcss"

interface IButton {
  className?: string
  classNameBody?: string
  classNameIcon?: string
  children?: React.ReactNode
  type?: "button" | "submit" | "reset"
  icon?: string,
  iconPosition?: "before" | "after"
  onClick?: (event: React.MouseEvent<HTMLButtonElement>) => void
}

function Button(
  {
    className,
	  classNameBody,
	  classNameIcon,
    children,
    type = "button",
    icon,
    iconPosition = "after",
    onClick
  }: IButton
) {
  const iconMarkup = useMemo(() => {
	  if (!icon) return null
	  return <SvgIcon className={classNames(classNameIcon, "button__body__icon")} name={icon} />
  }, [ icon ])

  const bodyMarkup = useMemo(() => {
	  return (
	    <span className={classNames(classNameBody, "button__body")}>
		    {(icon && iconPosition === "before") && iconMarkup}
		    {children && <span className="button__label">{children}</span>}
		    {(icon && iconPosition === "after") && iconMarkup}
	    </span>
	  )
  }, [ iconMarkup, iconPosition, children ])


  const args = {
	  className: classNames(className, "button"),
	  onClick: onClick
  }

  return <button {...args} type={type}>{bodyMarkup}</button>
}

export default Button
