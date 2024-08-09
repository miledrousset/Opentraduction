document.addEventListener("DOMContentLoaded", function() {
  var timeoutInMilliseconds = 2 * 60 * 60 * 1000; // 120 minute
  var timeoutUrl = "/shs-traduction/index.xhtml";

  setTimeout(function() {
    // Call logout method and then redirect to login page
    fetch('/shs-traduction/api/logout', { method: 'POST' })
        .then(response => {
          if (response.ok) {
            window.location.href = timeoutUrl;
          } else {
            console.error('Logout failed');
          }
        })
        .catch(error => console.error('Error during logout:', error));
  }, timeoutInMilliseconds);
});
