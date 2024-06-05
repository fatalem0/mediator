import { MainPageState } from "./MainPage"

type ACTIONTYPE =
	| { type: "no-popups"}
	| { type: "login-popup-open" }
	| { type: "register-popup-open" }

function mainPageStateReducer(state: MainPageState, action: ACTIONTYPE) {
	switch (action.type) {
		case "no-popups": {
			return {
				...state,
				noPopups: true,
				loginPopupOpen: false,
				registerPopupOpen: false
			}
		}

		case "login-popup-open": {
			return {
				...state,
				noPopups: false,
				loginPopupOpen: true,
				registerPopupOpen: false
			}
		}

		case "register-popup-open": {
			return {
				...state,
				noPopups: false,
				loginPopupOpen: false,
				registerPopupOpen: true
			}
		}
	}
}

export default mainPageStateReducer
