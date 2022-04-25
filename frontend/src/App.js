
import {
  BrowserRouter,
  Navigate,
  Route,
  Routes,
} from 'react-router-dom';
import Login from './components/Login/Login';
import Navigation from './components/Navigation/Navigation';
import { useCallback, useState } from 'react';
import Home from './components/Home/Home';
import { getLoggedInUser } from './utils/Storage/Storage';
import Logout from './components/Logout/Logout';

import "./App.css";

function App() {

  const[user, setUser] = useState(getLoggedInUser());

  const PrivateRoute = ({ children }) => {
    if (user !== null)
    {
      return children;
    } else {
      return <Navigate to="/login" />;
    }
  }

  const setUserFromChild = useCallback(user => setUser(user), [setUser]);

  return (
    <BrowserRouter>
      <Routes>
        <Route element={ <Navigation user={user} />} >
          <Route path="/login" element={<Login user={user} setUser={setUserFromChild} />} /> 
          <Route path="/home" element={<PrivateRoute><Home /></PrivateRoute>} />
          <Route path="/logout" element={<Logout setUser={setUserFromChild} />} />
          <Route path="*" element={<Navigate to="/home" />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
