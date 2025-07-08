document.getElementById('registerForm').addEventListener('submit', async function(event) {
    event.preventDefault();

    const data = {
        username: document.getElementById('username').value.trim(),
        name: document.getElementById('name').value.trim(),
        password: document.getElementById('password').value,
        email: document.getElementById('email').value.trim(),
        role: document.getElementById('role').value
    };

    try {
        const response = await fetch('/api/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });

        const messageDiv = document.getElementById('registerMessage');
        if (response.ok) {
            messageDiv.style.color = "green";
            messageDiv.textContent = "Registration successful! You can now log in.";
        } else {
            const errorText = await response.text();
            messageDiv.style.color = "red";
            messageDiv.textContent = errorText;
        }
    } catch (err) {
        document.getElementById('registerMessage').textContent = "Error connecting to server.";
    }
});
