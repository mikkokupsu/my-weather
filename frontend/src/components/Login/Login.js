import React, { useState } from 'react';
import './Login.css';

import Button from "react-bootstrap/Button";
import Form from "react-bootstrap/Form";
import { getLoggedInUser, storeLoggedInUser } from '../../utils/Storage/Storage';
import { Navigate } from 'react-router-dom';
import { API_URL } from '../../utils/Api/Api';

export default function Login(props) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  function validateForm() {
    return username.length > 0 && password.length > 0;
  }

  async function loginUser(username, password) {
    return fetch(`${API_URL}/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({'username': username, 'password': password})
    })
    .then(async response => {
      if (!response.ok) {
        setError('Failed to login');
      } else {
        const data = await response.json();
        storeLoggedInUser(data.accessToken);
      }
    })
    .catch(reason => setError(reason));
}

  function handleSubmit(event) {
    event.preventDefault();
    loginUser(username, password).finally(() => {
      const user = getLoggedInUser();
      props.setUser(user);
    });
  }

  if (props.user !== null) {
    return (<Navigate to="/home" />);
  }

  const LoginFailed = () => (
    <div class="alert alert-warning" role="alert">
      {error}
    </div>
  )

  return (
    <div className="Login">
      <Form onSubmit={handleSubmit}>
        { error.trim() !== "" ? <LoginFailed /> : null }
        <Form.Group size="lg" className="mb-3" controlId="username">
          <Form.Label>Email</Form.Label>
          <Form.Control autoFocus type="username" value={username} onChange={(e) => setUsername(e.target.value)} />
        </Form.Group>
        <Form.Group size="lg" className="mb-3" controlId="password">
          <Form.Label>Password</Form.Label>
          <Form.Control type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
        </Form.Group>
        <Button block size="lg" className="mb-3" type="submit" disabled={!validateForm()}>
          Login
        </Button>
      </Form>
    </div>
  );
}