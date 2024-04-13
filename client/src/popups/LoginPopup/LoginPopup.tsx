import { Link } from "react-router-dom"
import Popup from "../../components/views/Popup/Popup"
import UserForm from "../../components/views/UserForm/UserForm"
import "./LoginPopup.pcss"
import { AppRoutes } from "../../types/const"

interface ILoginPopup {
  isLoginPopupOpen: boolean
  onClose: () => void
}

function LoginPopup({ isLoginPopupOpen, onClose }: ILoginPopup) {
  return (
    <Popup isOpen={isLoginPopupOpen}>
	   <UserForm
	      className="login-popup__form"
		    classNameCloseButton="login-popup__form__close_button"
        classNameCloseButtonBody="login-popup__form__close_button__body"
        submitButtonLabel="Войти"
        notice={<>Нет аккаунта? <Link to={AppRoutes.register}>Зарегистрироваться</Link></>}
        onClose={onClose}
      />
    </Popup>
  )
}

export default LoginPopup
