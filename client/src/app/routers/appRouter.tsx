import { Route, RouterProvider, createBrowserRouter, createRoutesFromElements } from "react-router-dom"
import { AppRoutes } from "./endpoints"
import MainPage from "../../pages/main/MainPage"
import WarningPage from "../../pages/warning/WarningPage"
import RegisterPage from "../../pages/register/RegisterPage"
import { FeedPage } from "../../pages/feed"
import { AccountEditPage } from "../../pages/accountEdit"
import { MessengerPage } from "../../pages/messenger"

export const AppRouter = () => {
	const routers = createRoutesFromElements(
		<>
			<Route path={AppRoutes.main} element={<MainPage />} />
			<Route path={AppRoutes.warning} element={<WarningPage />} />
			<Route path={AppRoutes.register} element={<RegisterPage />} />
			<Route path={AppRoutes.feed} element={<FeedPage />} />
			<Route path={AppRoutes.accountEdit} element={<AccountEditPage />} />
			<Route path={AppRoutes.messenger} element={<MessengerPage />} />
		</>
	)

	const router = createBrowserRouter(routers, {})

	return <RouterProvider router={router} />
}
