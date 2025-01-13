document.addEventListener("DOMContentLoaded", function() {
  var timeoutInMilliseconds = 120 * 60 * 60 * 1000; // 120 minute
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

document.addEventListener('DOMContentLoaded', function() {
    if (document.documentElement.getAttribute('data-session-invalid') === 'true') {
        fetch('/api/logout', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        }).then(response => {
            if (response.ok) {
                window.location.href = '/index.xhtml';
            } else {
                console.error('La requête de déconnexion a échoué.');
            }
        }).catch(error => {
            console.error('Une erreur est survenue lors de la requête de déconnexion :', error);
        });
    }
});
