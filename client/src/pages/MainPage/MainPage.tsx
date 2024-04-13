import { useState } from "react"
import Button from "../../components/UI/Button/Button"
import PageTitle from "../../components/UI/PageTitle/PageTitle"
import SvgIcon from "../../components/UI/SvgIcon/SvgIcon"
import Content from "../../components/views/Content/Content"
import LoginPopup from "../../popups/LoginPopup/LoginPopup"
import "./MainPage.pcss"

function MainPage() {
  const [isLoginPopupOpen, setIsLoginPopupOpen] = useState(false)

  return (
    <Content className="main-page" classNameBody="main-page__body">
      <SvgIcon className="main-page__logo" name="mediator-logo"></SvgIcon>
      <PageTitle className="main-page__title">Mediator</PageTitle>
      <Button
        className="main-page__button"
        classNameBody="main-page__button__body"
        onClick={() => setIsLoginPopupOpen(true)}
      >
        Войти
      </Button>
      {isLoginPopupOpen &&
        <LoginPopup isLoginPopupOpen={isLoginPopupOpen} onClose={() => setIsLoginPopupOpen(false)}/>
      }
    </Content>
  )
}

export default MainPage
