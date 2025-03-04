import React, { useEffect, useState } from "react";
import { Card, Button, Row, Col, InputGroup, Form } from "react-bootstrap";
import { PersonPlus, Search } from "react-bootstrap-icons";
import { UserResponseModel } from "../../../../users/model/users.model";
import { useUsersApi } from "../../../../users/api/users.api";
import UsersList from "../../../../users/components/UsersList";

const UserManagement: React.FC = () => {
  const { getAllUsers, updateUser, updateUserRole, syncUser } = useUsersApi();
  const [users, setUsers] = useState<UserResponseModel[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedRole, setSelectedRole] = useState("All Roles");

  const fetchAllUsers = async () => {
    try {
      setLoading(true);
      const data = await getAllUsers();
      setUsers(data || []);
      setError(null);
    } catch (err) {
      setError("Failed to fetch users.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAllUsers();
  }, []);

  const handleUpdateUser = async (
    userId: string,
    updatedUser: Partial<UserResponseModel>
  ) => {
    try {
      setLoading(true);
      const userUpdateData = {
        firstName: updatedUser.firstName,
        lastName: updatedUser.lastName,
        email: updatedUser.email,
      };

      await updateUser(userId, userUpdateData);
      await fetchAllUsers();
    } catch (error) {
      setError("Failed to update user");
      console.error("Failed to update user", error);
    } finally {
      setLoading(false);
    }
  };

  const handleRoleUpdate = async (userId: string, roleIds: string[]) => {
    try {
      setLoading(true);
      await updateUserRole(userId, roleIds);
      await syncUser(userId);
      await fetchAllUsers();
    } catch (error) {
      setError("Failed to update user role");
      console.error("Failed to update user role", error);
    } finally {
      setLoading(false);
    }
  };


  if (loading) return <div>Loading...</div>;
  if (error) return <div className="alert alert-danger">{error}</div>;

  return (
    <div
      className="d-flex justify-content-center align-items-start p-4"
      style={{ backgroundColor: "#f8f9fa", minHeight: "100vh" }}
    >
      <Card
        className="rounded shadow border-0"
        style={{ width: "100%", maxWidth: "1600px" }}
      >
        <Card.Body>
          <div className="d-flex justify-content-between align-items-center mb-4">
            <h2 className="mb-0">User Management</h2>
            <Button variant="primary">
              <PersonPlus className="me-2" />
              Add New User
            </Button>
          </div>

          <Row className="mb-4 g-3">
            <Col md={4}>
              <InputGroup>
                <Form.Control
                  placeholder="Search users..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
                <Button variant="outline-secondary">
                  <Search />
                </Button>
              </InputGroup>
            </Col>
            <Col md={3}>
              <Form.Select
                value={selectedRole}
                onChange={(e) => setSelectedRole(e.target.value)}
              >
                <option>All Roles</option>
                <option>Admin</option>
                <option>Customer</option>
                <option>Employee</option>
              </Form.Select>
            </Col>
          </Row>

          <UsersList
            users={users}
            onUpdateUser={handleUpdateUser}
            onUpdateRole={handleRoleUpdate}
          />

          <div className="d-flex justify-content-between align-items-center mt-4">
            <div>
              <span className="text-muted">
                Showing {users.length} of {users.length} entries
              </span>
            </div>
          </div>
        </Card.Body>
      </Card>
    </div>
  );
};

export default UserManagement;
