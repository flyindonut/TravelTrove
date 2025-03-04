import React, { useEffect, useState } from "react";
import { Button, Table, Modal, Form, Card, Collapse, Row, Col } from "react-bootstrap"; // Import Row and Col
import { useTranslation } from "react-i18next";
import { useSearchParams } from "react-router-dom";
import { useBookingsApi } from "../../../bookings/api/bookings.api";
import { useUsersApi } from "../../../users/api/users.api"; // Import useUsersApi
import { useTravelersApi } from "../../../travelers/api/traveler.api"; // Import useTravelersApi
import {
  BookingRequestModel,
  BookingResponseModel,
  BookingStatus,
} from "../../../bookings/models/bookings.model";
import "./Bookings.css";
import "../../../../shared/css/Scrollbar.css";

const formatStatus = (status: string) => {
  return status
    .toLowerCase()
    .split('_')
    .map(word => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');
};

const Bookings: React.FC = () => {
  const { t } = useTranslation();
  const [searchParams] = useSearchParams();
  const packageId = searchParams.get("packageId");
  const { getAllBookings, updateBookingStatus } = useBookingsApi();
  const { getUserById } = useUsersApi(); // Get user by ID
  const { getTravelerById } = useTravelersApi(); // Get traveler by ID
  const [bookings, setBookings] = useState<BookingResponseModel[]>([]);
  const [showModal, setShowModal] = useState(false);
  const [modalType, setModalType] = useState<"updateStatus" | "view">("updateStatus");
  const [selectedBooking, setSelectedBooking] =
    useState<BookingResponseModel | null>(null);
  const [formData, setFormData] = useState<BookingRequestModel>({
    userId: "",
    packageId: packageId || "",
    totalPrice: 0,
    status: BookingStatus.PAYMENT_PENDING,
    bookingDate: new Date().toISOString().split("T")[0],
    travelers: [],
  });
  const [formErrors, setFormErrors] = useState({
    userId: false,
    totalPrice: false,
    status: false,
    bookingDate: false,
  });
  const [userNames, setUserNames] = useState<{ [key: string]: string }>({}); // State for user names
  const [openBookingId, setOpenBookingId] = useState<string | null>(null); // State for toggling travelers list
  const [travelers, setTravelers] = useState<{ [key: string]: { name: string; email: string }[] }>({}); // State for travelers

  useEffect(() => {
    fetchBookings();
  }, [packageId]);

  const fetchBookings = async () => {
    try {
      const data = await getAllBookings({ packageId: packageId || undefined });
      const userIds = data.map((booking) => booking.userId);
      const userNamesData = await Promise.all(userIds.map((id) => getUserById(id)));
      const userNamesMap = userNamesData.reduce((acc, user) => {
        acc[user.userId] = user.firstName + " " + user.lastName; // Assuming firstName and lastName exist
        return acc;
      }, {} as { [key: string]: string });
      setUserNames(userNamesMap);
      setBookings(data);
    } catch (error) {
      console.error("Error fetching bookings:", error);
    }
  };

  const fetchTravelers = async (travelerIds: string[]) => {
    try {
      const travelersData = await Promise.all(travelerIds.map((id) => getTravelerById(id)));
      console.log("Fetched travelers:", travelersData); // Log fetched travelers
      return travelersData.map((traveler) => ({
        name: `${traveler.firstName} ${traveler.lastName}`,
        email: traveler.email,
      }));
    } catch (error) {
      console.error("Error fetching travelers:", error);
      return [];
    }
  };

  const handleSave = async () => {
    const errors = {
      userId: !formData.userId,
      totalPrice: formData.totalPrice === null,
      status: !formData.status,
      bookingDate: !formData.bookingDate,
    };
    setFormErrors(errors);

    if (Object.values(errors).some((error) => error)) {
      return;
    }

    try {
      if (modalType === "updateStatus" && selectedBooking) {
        await updateBookingStatus(selectedBooking.bookingId, formData.status);
      }
      setShowModal(false);
      await fetchBookings();
    } catch (error) {
      console.error("Error saving booking:", error);
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    handleSave();
  };

  const toggleTravelersList = async (bookingId: string, travelerIds: string[]) => {
    if (openBookingId === bookingId) {
      setOpenBookingId(null);
    } else {
      if (!travelers[bookingId]) {
        const travelersData = await fetchTravelers(travelerIds);
        setTravelers((prev) => ({ ...prev, [bookingId]: travelersData }));
      }
      setOpenBookingId(bookingId);
    }
  };

  return (
    <div
      className="d-flex justify-content-center align-items-center p-4 dashboard-scrollbar"
      style={{ backgroundColor: "#f8f9fa", minHeight: "100vh" }}
    >
      <Card
        className="rounded shadow border-0"
        style={{
          width: "1600px",
          height: "800px",
          borderRadius: "15px",
          overflow: "hidden",
        }}
      >
        <Card.Body className="p-4">
          <div className="d-flex justify-content-between align-items-center mb-3">
            <h3>{t("Bookings")}</h3>
          </div>

          <Card className="rounded shadow-sm border-0">
            <Card.Body>
              <Table bordered hover responsive className="rounded custom-table">
                <thead className="bg-light">
                  <tr>
                    <th>{t("User Name")}</th>
                    <th>{t("Total Price")}</th>
                    <th>{t("Status")}</th>
                    <th>{t("Booking Date")}</th>
                    <th>{t("Actions")}</th>
                  </tr>
                </thead>
                <tbody>
                  {bookings.map((booking) => (
                    <React.Fragment key={booking.bookingId}>
                      <tr>
                        <td>{userNames[booking.userId]}</td> {/* Display user name */}
                        <td>{booking.totalPrice}</td>
                        <td>{formatStatus(booking.status)}</td> {/* Format status */}
                        <td>{booking.bookingDate}</td>
                        <td>
                          {booking.status !== BookingStatus.REFUNDED && (
                            <Button
                              variant="outline-primary"
                              onClick={() => {
                                setSelectedBooking(booking);
                                setModalType("updateStatus");
                                setFormData({
                                  userId: booking.userId,
                                  packageId: booking.packageId,
                                  totalPrice: booking.totalPrice,
                                  status: booking.status,
                                  bookingDate: booking.bookingDate,
                                  travelers: [],
                                });
                                setFormErrors({
                                  userId: false,
                                  totalPrice: false,
                                  status: false,
                                  bookingDate: false,
                                });
                                setShowModal(true);
                              }}
                            >
                              {t("Update Status")}
                            </Button>
                          )}
                          <Button
                            variant="outline-secondary"
                            onClick={() => booking.travelerIds && toggleTravelersList(booking.bookingId, booking.travelerIds)}
                            className="ml-2"
                          >
                            {openBookingId === booking.bookingId ? t("Hide Travelers") : t("Show Travelers")}
                          </Button>
                        </td>
                      </tr>
                      <tr>
                        <td colSpan={5} className="p-0">
                          <Collapse in={openBookingId === booking.bookingId}>
                            <div className="p-3">
                              <div className="travelers-grid">
                                {travelers[booking.bookingId]?.map((traveler) => (
                                  <Card key={traveler.email} className="traveler-card">
                                    <Card.Body>
                                      <Card.Title>{traveler.name}</Card.Title>
                                      <Card.Text>{traveler.email}</Card.Text>
                                    </Card.Body>
                                  </Card>
                                ))}
                              </div>
                            </div>
                          </Collapse>
                        </td>
                      </tr>
                    </React.Fragment>
                  ))}
                </tbody>
              </Table>
            </Card.Body>
          </Card>

          <Modal
            show={showModal}
            onHide={() => setShowModal(false)}
            className="rounded"
          >
            <Modal.Header closeButton>
              <Modal.Title>
                {modalType === "updateStatus" ? t("Update Booking Status") : t("View Booking")}
              </Modal.Title>
            </Modal.Header>
            <Modal.Body>
              {modalType === "view" ? (
                <div>
                  <p>
                    <strong>{t("User Name")}:</strong> {userNames[selectedBooking?.userId || ""]}
                  </p>
                  <p>
                    <strong>{t("Total Price")}:</strong>{" "}
                    {selectedBooking?.totalPrice}
                  </p>
                  <p>
                    <strong>{t("Status")}:</strong>{" "}
                    {selectedBooking?.status ? formatStatus(selectedBooking.status) : ""}
                  </p>
                  <p>
                    <strong>{t("Booking Date")}:</strong>{" "}
                    {selectedBooking?.bookingDate}
                  </p>
                </div>
              ) : (
                <Form onSubmit={handleSubmit}>
                  <Form.Group className="mb-3">
                    <Form.Label>{t("Status")}</Form.Label>
                    <Form.Control
                      as="select"
                      value={formData.status}
                      onChange={(e) =>
                        setFormData({
                          ...formData,
                          status: e.target.value as BookingStatus,
                        })
                      }
                      isInvalid={formErrors.status}
                    >
                      {/* Always show the current status as the placeholder */}
                      <option value={selectedBooking?.status} disabled>
                        {formatStatus(selectedBooking?.status || "Select Status")}
                      </option>

                      {/* If status is COMPLETED, only allow REFUNDED */}
                      {selectedBooking?.status === BookingStatus.BOOKING_CONFIRMED ? (
                        <option value={BookingStatus.REFUNDED}>{formatStatus(BookingStatus.REFUNDED)}</option>
                      ) : (
                        Object.values(BookingStatus)
                          .filter((status) =>
                            status !== BookingStatus.REFUNDED &&
                            !(selectedBooking?.status === BookingStatus.PAYMENT_ATTEMPT2_PENDING && status === BookingStatus.PAYMENT_PENDING)
                          )
                          .map((status) => (
                            <option key={status} value={status}>
                              {formatStatus(status)}
                            </option>
                          ))
                      )}
                    </Form.Control>
                    <Form.Control.Feedback type="invalid">
                      {t("Status is required")}
                    </Form.Control.Feedback>
                  </Form.Group>
                  <Modal.Footer>
                    <Button
                      variant="secondary"
                      onClick={() => setShowModal(false)}
                    >
                      {t("Cancel")}
                    </Button>
                    <Button type="submit" variant="primary">
                      {t("Save")}
                    </Button>
                  </Modal.Footer>
                </Form>
              )}
            </Modal.Body>
          </Modal>
        </Card.Body>
      </Card>
    </div>
  );
};

export default Bookings;
