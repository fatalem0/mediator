import { useReducer } from "react"
import registerPageStateReducer from "./registerPageStateReducer"
import { ChooseFavoriteGenresPage } from "../registration/chooseFavoriteGenres"
import { ChooseUserPurposesPage } from "../registration/chooseUserPurposes"
import { ChooseUserFavoriteArtistsPage } from "../registration/chooseUserFavoriteArtists"
import { CreateUserDataPage } from "../registration/createUserData"

export interface RegisterPageState {
	chooseUserPurposesOpen: boolean
	chooseUserFavoriteGenresOpen: boolean
	chooseUserFavoriteArtistsOpen: boolean
	chooseUserCityAndAccountNameOpen: boolean
}

function RegisterPage() {
	const initRegisterPageState: RegisterPageState = {
		chooseUserPurposesOpen: true,
		chooseUserFavoriteGenresOpen: false,
		chooseUserFavoriteArtistsOpen: false,
		chooseUserCityAndAccountNameOpen: false
	}

	const [registerPageState, dispatch] = useReducer(registerPageStateReducer, initRegisterPageState)

	function handleOnChooseUserPurposesOpen() {
		dispatch({
			type: "choose-user-purposes-open"
		})
	}

	function handleOnChooseUserFavoriteGenresOpen() {
		dispatch({
			type: "choose-user-favorite-genres-open"
		})
	}

	function handleOnChooseUserFavoriteArtistsOpen() {
		dispatch({
			type: "choose-user-favorite-artists-open"
		})
	}

	function handleOnChooseUserCityAndAccountNameOpen() {
		dispatch({
			type: "choose-user-city-and-account-name-open"
		})
	}

	return (
		<>
			{/* {registerPageState.chooseUserPurposesOpen &&
				<ChooseUserPurposesPage handleOnChooseUserFavoriteGenresOpen={handleOnChooseUserFavoriteGenresOpen} />
			}
			{registerPageState.chooseUserFavoriteGenresOpen &&
				<ChooseFavoriteGenresPage
					handleOnBackButtonClick={handleOnChooseUserPurposesOpen}
					handleOnChooseUserFavoriteArtistsOpen={handleOnChooseUserFavoriteArtistsOpen}
				/>
			}
			{registerPageState.chooseUserFavoriteArtistsOpen &&
				<ChooseUserFavoriteArtistsPage
					handleOnBackButtonClick={handleOnChooseUserFavoriteArtistsOpen}
					handleOnChooseUserCityAndAccountNameOpen={handleOnChooseUserCityAndAccountNameOpen}
				/>
			} */}
			{/* {registerPageState.chooseUserCityAndAccountNameOpen && */}
				<CreateUserDataPage
					handleOnBackButtonClick={handleOnChooseUserFavoriteArtistsOpen}
				/>
			{/* } */}
		</>
	)
}

export default RegisterPage
