import { useReducer, useState } from "react"
import Button from "../../components/UI/Button/Button.tsx"
import PageTitle from "../../components/UI/PageTitle/PageTitle.tsx"
import SvgIcon from "../../components/UI/SvgIcon/SvgIcon.tsx"
import Content from "../../components/views/Content/Content.tsx"
import LoginPopup from "../../popups/LoginPopup/LoginPopup.tsx"
import RegisterPopup from "../../popups/RegisterPopup/RegisterPopup.tsx"
import mainPageStateReducer from "./mainPageStateReducer.ts"
import "./mainPage.pcss"

export interface MainPageState {
	noPopups: boolean
	loginPopupOpen: boolean
	registerPopupOpen: boolean
}

function MainPage() {
	const initMainPageState: MainPageState = {
		noPopups: true,
		loginPopupOpen: false,
		registerPopupOpen: false
	}

	const [mainPageState, dispatch] = useReducer(mainPageStateReducer, initMainPageState)

	function handleOnClose() {
		dispatch({
			type: "no-popups"
		})
	}

	function handleOnLoginPopupOpen() {
		dispatch({
			type: "login-popup-open"
		})
	}

	function handleOnRegisterPopupOpen() {
		dispatch({
			type: "register-popup-open"
		})
	}

	return (
		<Content className="main-page" classNameBody="main-page__body">
			<SvgIcon className="main-page__logo" name="mediator-logo"></SvgIcon>
			<PageTitle className="main-page__title">Mediator</PageTitle>
			<Button
				className="main-page__button"
				classNameBody="main-page__button__body"
				onClick={handleOnLoginPopupOpen}
			>
				Войти
			</Button>
			<LoginPopup
				isLoginPopupOpen={mainPageState.loginPopupOpen}
				handleOnRegisterPopupOpen={handleOnRegisterPopupOpen}
				onClose={handleOnClose}
			/>
			<RegisterPopup
				isRegisterPopupOpen={mainPageState.registerPopupOpen}
				onClose={() => handleOnClose()}
			/>
		</Content>
	)
}

export default MainPage
