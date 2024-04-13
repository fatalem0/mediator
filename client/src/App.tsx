import { createBrowserRouter } from "react-router-dom"
import { AppRoutes } from "./types/const"
import MainPage from "./pages/MainPage/MainPage"
import WarningPage from "./pages/WarningPage/WarningPage"
import RegisterPage from "./pages/RegisterPage/RegisterPage"

export const router =
  createBrowserRouter([
    {
      path: AppRoutes.main,
      element: <MainPage />
    },
    {
      path: AppRoutes.warning,
      element: <WarningPage />
    },
    {
      path: AppRoutes.register,
      element: <RegisterPage />
    }
  ]
)
