import classNames from "classnames"
import "./Container.pcss"

interface IContainer {
	className?: string
	children: React.ReactNode
}

function Container({ className, children }: IContainer) {
	return (
		<div className={classNames(className, "container")}>
			{children}
		</div>
	)
}

export default Container
