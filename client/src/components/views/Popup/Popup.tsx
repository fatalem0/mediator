import classNames from "classnames"
import Content from "../Content/Content"
import "./Popup.pcss"

interface IPopup {
	classNameBody?: string
	children: React.ReactNode
	isOpen: boolean
}

function Popup({ classNameBody, children, isOpen }: IPopup) {
	return (
	<>
		{isOpen && (
		<Content className="popup" classNameBody={classNames(classNameBody, "popup__body")}>
			{children}
		</Content>
		)}
	</>
	)
}

export default Popup
