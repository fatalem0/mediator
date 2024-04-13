import Content from "../Content/Content"
import "./Popup.pcss"

interface IPopup {
  children: React.ReactNode,
  isOpen: boolean
}

function Popup({ children, isOpen }: IPopup) {
  return (
	<>
	  {isOpen && (
		<Content className="popup" classNameBody="popup__body">
		  {children}
		</Content>
	  )}
	</>
  )
}

export default Popup
