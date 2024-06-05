import { RegisterPageState } from "./RegisterPage"

type ACTIONTYPE =
	| { type: "choose-user-purposes-open"}
	| { type: "choose-user-favorite-genres-open" }
	| { type: "choose-user-favorite-artists-open" }
	| { type: "choose-user-city-and-account-name-open" }

function registerPageStateReducer(state: RegisterPageState, action: ACTIONTYPE) {
	switch (action.type) {
		case "choose-user-purposes-open": {
			return {
				...state,
				chooseUserPurposesOpen: true,
				chooseUserFavoriteGenresOpen: false,
				chooseUserFavoriteArtistsOpen: false,
				chooseUserCityAndAccountNameOpen: false
			}
		}

		case "choose-user-favorite-genres-open": {
			return {
				...state,
				chooseUserPurposesOpen: false,
				chooseUserFavoriteGenresOpen: true,
				chooseUserFavoriteArtistsOpen: false,
				chooseUserCityAndAccountNameOpen: false
			}
		}

		case "choose-user-favorite-artists-open": {
			return {
				...state,
				chooseUserPurposesOpen: false,
				chooseUserFavoriteGenresOpen: false,
				chooseUserFavoriteArtistsOpen: true,
				chooseUserCityAndAccountNameOpen: false
			}
		}

		case "choose-user-city-and-account-name-open": {
			return {
				...state,
				chooseUserPurposesOpen: false,
				chooseUserFavoriteGenresOpen: false,
				chooseUserFavoriteArtistsOpen: false,
				chooseUserCityAndAccountNameOpen: true
			}
		}
	}
}

export default registerPageStateReducer
