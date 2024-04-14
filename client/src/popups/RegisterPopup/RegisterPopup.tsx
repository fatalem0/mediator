import Popup from "../../components/views/Popup/Popup"
import RegisterForm from "../../forms/RegisterForm/RegisterForm"
import "./RegisterPopup.pcss"

interface IRegisterPopup {
	isRegisterPopupOpen: boolean
	onClose: () => void
}

function RegisterPopup({ isRegisterPopupOpen, onClose }: IRegisterPopup) {
	return (
		<Popup classNameBody="register-popup" isOpen={isRegisterPopupOpen}>
			<RegisterForm
				submitButtonLabel="Создать"
				onClose={onClose}
			/>
		</Popup>
	)
}

export default RegisterPopup
