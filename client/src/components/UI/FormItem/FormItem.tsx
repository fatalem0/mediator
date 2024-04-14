import classNames from "classnames"
import { ReactNode } from "react"
import "./FormItem.pcss"

interface IFormItem {
	className?: string
	children: ReactNode
}

function FormItem({ className, children }: IFormItem) {
	return (
		<div className={classNames(className, "form-item")}>
			{children}
		</div>
	)
}

export default FormItem
