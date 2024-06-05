import React from "react"
import ReactDOM from "react-dom/client"
import "./styles"
import "virtual:svg-icons-register"
import "react-chat-elements/dist/main.css"
import App from "./app/App"

ReactDOM.createRoot(document.getElementById('root')!).render(
	<React.StrictMode>
		<div className="app">
			<App />
			{/* <QueryClientProvider client={queryClient}> */}
				{/* <RouterProvider router={router} /> */}
				{/* <BrowserRouter>
					<App />
				</BrowserRouter> */}
			{/* </QueryClientProvider> */}
		</div>
	</React.StrictMode>,
)
