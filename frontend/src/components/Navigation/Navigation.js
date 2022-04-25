import {
    Container,
    Nav,
    Navbar,
 } from 'react-bootstrap';
import { Outlet } from 'react-router-dom';

export default function Navigation(props) {
    const loggedIn = props.user !== null;

    return (<>
    <Navbar bg="light" expand="lg">
        <Container>
            <Navbar.Brand href="/home">MyWeather</Navbar.Brand>
            <Navbar.Toggle aria-controls="basic-navbar-nav" />
            <Navbar.Collapse id="basic-navbar-nav">
            <Nav className="me-auto">
                <Nav.Link href="/home">Home</Nav.Link>
            </Nav>
            <Nav>
                {loggedIn ? <Nav.Link href="/logout">Logout</Nav.Link> : <Nav.Link href="/login">Login</Nav.Link>}
            </Nav>
            </Navbar.Collapse>
        </Container>
    </Navbar>
    <Outlet />
    </>);
}