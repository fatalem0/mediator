import classNames from "classnames"
import "./Label.pcss"

interface ILabel {
  className?: string
  children: React.ReactNode
  htmlFor?: string
}

function Label({className, children, htmlFor = ""}: ILabel) {
  return (
	<label
	  className={classNames(className, "label")}
	  htmlFor={htmlFor}
	>
	  {children}
	</label>
  )
}

export default Label
