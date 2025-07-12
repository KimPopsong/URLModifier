<template>
  <div id="app">
    <h1>URL Shortener</h1>
    <form @submit.prevent="shortenUrl">
      <input type="text" v-model="originalUrl" placeholder="Enter URL to shorten">
      <button type="submit">Shorten</button>
    </form>
    <div v-if="shortenedUrl" class="result">
      <h2>Shortened URL:</h2>
      <a :href="shortenedUrl" target="_blank">{{ shortenedUrl }}</a>
      <img :src="qrCode" alt="QR Code">
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      originalUrl: '',
      shortenedUrl: '',
      qrCode: ''
    };
  },
  methods: {
    async shortenUrl() {
      try {
        const response = await axios.post('http://localhost:8080/short-urls', {
          url: this.originalUrl
        });
        this.shortenedUrl = response.data.shortenedUrl;
        this.qrCode = response.data.qrCode;
      } catch (error) {
        console.error(error);
      }
    }
  }
};
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}

form {
  margin-bottom: 20px;
}

input {
  padding: 10px;
  width: 300px;
  margin-right: 10px;
}

button {
  padding: 10px;
}

.result {
  margin-top: 20px;
}

img {
  margin-top: 20px;
  display: block;
  margin-left: auto;
  margin-right: auto;
}
</style>