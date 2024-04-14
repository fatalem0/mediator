import React from "react"
import ReactDOM from "react-dom/client"
import { RouterProvider } from "react-router-dom"
import { router } from "./App"
import "./styles"
import "virtual:svg-icons-register"

ReactDOM.createRoot(document.getElementById('root')!).render(
	<React.StrictMode>
		<div className="app">
			<RouterProvider router={router} />
		</div>
	</React.StrictMode>,
)
