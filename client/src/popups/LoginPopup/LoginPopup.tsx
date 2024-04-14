import Popup from "../../components/views/Popup/Popup"
import LoginForm from "../../forms/LoginForm/LoginForm"
import "./LoginPopup.pcss"

interface ILoginPopup {
	isLoginPopupOpen: boolean
	handleOnRegisterPopupOpen: () => void
	onClose: () => void
}

function LoginPopup({ isLoginPopupOpen, handleOnRegisterPopupOpen, onClose }: ILoginPopup) {
	return (
		<Popup isOpen={isLoginPopupOpen}>
			<LoginForm
				submitButtonLabel="Войти"
				registerButtonLabel="Создать аккаунт"
				handleOnRegisterPopupOpen={handleOnRegisterPopupOpen}
				onClose={onClose}
			/>
		</Popup>
	)
}

export default LoginPopup
