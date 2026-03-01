FROM node:22-alpine
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
<<<<<<< Updated upstream
CMD ["npm", "start", "--", "--host", "0.0.0.0"]
=======
RUN npm run build --configuration=production

FROM nginx:alpine
# Kopieer de custom nginx configuratie
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
>>>>>>> Stashed changes
