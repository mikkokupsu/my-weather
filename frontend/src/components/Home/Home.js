import DateTimePicker from 'react-datetime-picker';
import { Button, Form, Modal, Table } from "react-bootstrap";
import { useCallback, useEffect, useState } from "react";
import { deleteTemperatureAir, getTemperatureAir, postTemperatureAir, putTemperatureAir } from "../../utils/Api/Api";
import moment from "moment";

import "./Home.css";

export default function Home() {

    const [measurements, setMeasurements] = useState([]);

    const [modalShow, setModalShow] = useState(false);
    const [modalMeasurement, setModalMeasurement] = useState({});

    const [startDate, setStartDate] = useState(new Date("2022-02-01"));
    const [stopDate, setStopDate] = useState(new Date("2022-03-01"));
    const [location, setLocation] = useState("Kaisaniemi");
    const [showCelcius, setShowCelcius] = useState(true);

    const fetchData = useCallback((loc, start, stop) => {
        getTemperatureAir(loc, start, stop)
            .then(async response => {
                const data = await response.json();
                setMeasurements(data.map((value, index) => {
                    value.index = index;
                    return value;
                }));
            });
    }, []);

    useEffect(() => {
       fetchData(location, startDate, stopDate);
    }, [fetchData, location, startDate, stopDate]);

    const onEdit = (meas) => {
        setModalMeasurement(meas);
        setModalShow(true);
    }

    const onNew = () => {
        setModalMeasurement({});
        setModalShow(true);
    }

    const onChangeStartTime = (date) => {
        setStartDate(date);
        fetchData(date, stopDate);
    }

    const onChangeStopTime = (date) => {
        setStopDate(date);
        fetchData(startDate, date);
    }

    const editRow = (meas) => {
        return (
            <div align="right">
                <Button variant="link" onClick={() => onEdit(meas)}>edit</Button>
            </div>
        )
    }

    const createRow = (meas) => {
        const value = Math.round((showCelcius ? meas.value : (1.8 * meas.value + 32)) * 100) / 100;
        return (
            <tr>
                <td>{meas.location}</td>
                <td>{meas.timestamp}</td>
                <td style={{color: value >= 0 ? 'black' : 'red'}}>{value}</td>
                <td>
                    {editRow(meas)}   
                </td>

            </tr>
        )
    }

    const rows = measurements.map((meas) => createRow(meas));

    function MyVerticallyCenteredModal(props) {
        const [location, setLocation] = useState(modalMeasurement.location);
        const [timestamp, setTimestamp] = useState(modalMeasurement.timestamp);
        const [value, setValue] = useState(modalMeasurement.value);
        const [timestampValid, setTimestampValid] = useState(true);
        const newMeasurement = modalMeasurement.index === undefined;

        const saveMeas = () => {
            const date = moment(timestamp, moment.DATETIME_LOCAL_SECONDS, true);
            console.log('date ', date, date.isValid());
            if (!date.isValid()) {
                setTimestampValid(false);
                return;
            }

            setTimestampValid(true);

            const measurement = {
                "location": location,
                "timestamp": timestamp,
                "value": value,
            };

            let promise;
            if (newMeasurement) {
                promise = postTemperatureAir(measurement)
            } else {
                promise = putTemperatureAir(measurement);
            }

            promise.then(async (value) => {
                console.log('value', value);
                if (value.ok) {
                    const data = await value.json();
                    console.log(data);
                    modalMeasurement.value = data.value;
                    props.onHide();
                    fetchData(location, startDate, stopDate);
                }
            });
        }

        const deleteMeas = () => {
            const measurement = {
                "location": location,
                "timestamp": timestamp,
            };
            deleteTemperatureAir(measurement).then(async (value) => {
                if (value.ok) {
                    const data = await value.json();
                    setMeasurements(measurements.filter((item) => !(item.location === data.location && item.timestamp === data.timestamp)))
                    props.onHide();
                }
            });
        }

        const InvalidDateTime = () => (
            <div class="alert alert-warning" role="alert">
              Invalid timestamp value {timestamp}. Use format yyyy-mm-ddThh:mm:ss!
            </div>
          )

        return (
          <Modal
            {...props}
            size="lg"
            aria-labelledby="contained-modal-title-vcenter"
            centered    
          >
            <Modal.Header closeButton>
            </Modal.Header>
            <Modal.Body>
                { timestampValid ? <div /> : <InvalidDateTime /> }
                <Form>
                    <Form.Group className="mb-3" controlId="location">
                        <Form.Label>Location</Form.Label>
                        <Form.Control disabled={modalMeasurement.index !== undefined} placeholder="Location" value={location} onChange={(e) => setLocation(e.target.value)} />
                    </Form.Group>
                    <Form.Group className="mb-3" controlId="timestamp">
                        <Form.Label>Timestamp</Form.Label>
                        <Form.Control disabled={modalMeasurement.index !== undefined} placeholder="Timestamp" value={timestamp} onChange={(e) => {setTimestamp(e.target.value)}} />
                    </Form.Group>
                    <Form.Group className="mb-3" controlId="formBasicCheckbox">
                        <Form.Label>Value</Form.Label>
                        <Form.Control placeholder="Value" value={value} onChange={(e) => setValue(e.target.value)} />
                    </Form.Group>
                </Form>
            </Modal.Body>
            <Modal.Footer>
              <Button variant="danger" onClick={() => deleteMeas()}>Delete</Button>
              <Button variant="secondary" onClick={props.onHide}>Close</Button>
              <Button variant="primary" onClick={() => saveMeas()}>Save</Button>
            </Modal.Footer>
          </Modal>
        );
      }

    return (
        <>
        <label className='DateLabel'>Location</label>
        <select onChange={(e) => setLocation(e.target.value)} value={location}>
            <option value="Harmaja">Harmaja</option>
            <option value="Helsingin majakka">Helsingin majakka</option>
            <option value="Kaisaniemi">Kaisaniemi</option>
            <option value="Kumpula">Kumpula</option>
            <option value="Malmin lentokentta">Malmin lentokentta</option>
            <option value="Vuosaari satama">Vuosaari satama</option>
        </select>
        <span className='DateSeparator' />
        <label className='DateLabel'>Start timestamp</label>
        <DateTimePicker disableClock={true} format="y-MM-dd" value={startDate} onChange={onChangeStartTime} />
        <span className='DateSeparator' />
        <label className='DateLabel'>Stop timestamp</label>
        <DateTimePicker disableClock={true} format="y-MM-dd" value={stopDate} onChange={onChangeStopTime} />
        <span className='DateSeparator' />
        <label className='DateLabel'>Unit</label>
        <select onChange={(e) => setShowCelcius(e.target.value === "celsius")}>
            <option value="celsius">Celsius</option>
            <option value="fahreneit">Fahreneit</option>
        </select>
        <div>
            <Table striped bordered hover size="sm" >
                <thead>
                    <tr>
                        <th scope="col">Location</th>
                        <th scope="col">Timestamp</th>
                        <th scope="col">Air temperature {showCelcius ? "(C)" : "(F)"}</th>
                        <th scope="col"><div align="right"><Button variant="link" onClick={() => onNew()}>new</Button></div></th>
                    </tr>
                </thead>
                <tbody>
                    {rows}
                </tbody>
            </Table>
        </div>
        <MyVerticallyCenteredModal show={modalShow} onHide={() => setModalShow(false)} />
        </>
        );
}