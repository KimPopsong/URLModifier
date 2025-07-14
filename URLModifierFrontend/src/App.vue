<template>
  <div id="app" class="d-flex flex-column min-vh-100">
    <header class="bg-dark text-white text-center py-3">
      <h1>URL Modifier</h1>
    </header>

    <main class="flex-grow-1 container my-5">
      <b-card bg-variant="light" text-variant="dark" class="shadow-sm">
        <b-card-title class="text-center">URL Shortener</b-card-title>
        <b-form @submit.prevent="shortenUrl" class="my-4">
          <b-form-group label="Enter URL to shorten:" label-for="url-input">
            <b-form-input
              id="url-input"
              v-model="originalUrl"
              type="url"
              placeholder="https://example.com"
              required
              size="lg"
            ></b-form-input>
          </b-form-group>
          <div class="d-grid gap-2">
            <b-button type="submit" variant="primary" size="lg" :disabled="loading">
              <b-spinner small v-if="loading"></b-spinner>
              {{ loading ? 'Shortening...' : 'Shorten URL' }}
            </b-button>
          </div>
        </b-form>

        <div v-if="error" class="alert alert-danger mt-3">
          {{ error }}
        </div>

        <div v-if="shortenedUrl" class="result text-center mt-4">
          <h3>Your Shortened URL:</h3>
          <b-link :href="shortenedUrl" target="_blank">{{ shortenedUrl }}</b-link>
          <div class="mt-3">
            <b-img :src="qrCode" alt="QR Code" fluid thumbnail></b-img>
          </div>
           <b-button @click="copyToClipboard" variant="success" class="mt-3">Copy URL</b-button>
        </div>
      </b-card>
    </main>

    <footer class="bg-dark text-white text-center py-2 mt-auto">
      <p>&copy; 2025 URL Modifier</p>
    </footer>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      originalUrl: '',
      shortenedUrl: '',
      qrCode: '',
      loading: false,
      error: ''
    };
  },
  methods: {
    async shortenUrl() {
      this.loading = true;
      this.error = '';
      this.shortenedUrl = '';
      this.qrCode = '';
      try {
        // Assuming the backend is running on localhost:8080
        const response = await axios.post('http://localhost:8080/short-urls', {
          url: this.originalUrl
        });
        this.shortenedUrl = response.data.shortenedUrl;
        // Assuming the backend returns a base64 QR code string
        this.qrCode = `data:image/png;base64,${response.data.qrCode}`;
      } catch (err) {
        this.error = 'Failed to shorten URL. Please check the URL and try again.';
        console.error(err);
      } finally {
        this.loading = false;
      }
    },
    async copyToClipboard() {
      try {
        await navigator.clipboard.writeText(this.shortenedUrl);
        alert('URL copied to clipboard!');
      } catch (err) {
        alert('Failed to copy URL.');
        console.error('Failed to copy: ', err);
      }
    }
  }
};
</script>

<style>
/* Global styles */
body {
  background-color: #f8f9fa; /* Light gray background */
}

#app {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

/* Custom card styling */
.card {
  border: none;
  border-radius: 15px;
}

.card-title {
    font-weight: 300;
    font-size: 2rem;
}

/* Result styling */
.result h3 {
    font-weight: 300;
}

.result .btn {
    margin-top: 1rem;
}

/* Footer styling */
footer p {
    margin-bottom: 0;
}
</style>