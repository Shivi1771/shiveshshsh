<template>
  <div class="container mt-4">
    <header class="mb-4">
      <div class="d-flex justify-content-between align-items-center">
        <h1 class="display-4">Kochar Infotech LwM2M Management Platform</h1>
        <button class="btn btn-primary" @click="refreshPage">Refresh</button>
      </div>
    </header>

    <div class="row">
      <div class="col-lg-3 col-md-6 mb-3" v-for="(action, index) in actions" :key="index">
        <div class="card shadow-sm">
          <div class="card-body">
            <h5 class="card-title">
              <i :class="action.icon"></i> {{ action.title }}
            </h5>
            <div v-if="action.inputs">
              <input v-for="(input, idx) in action.inputs" :key="idx" type="text" v-model="input.value" class="form-control mb-2" :placeholder="input.placeholder">
            </div>
            <button class="btn btn-outline-primary" @click="action.method">Execute</button>
          </div>
        </div>
      </div>
    </div>

    <div class="mb-3">
      <div class="card shadow-sm">
        <div class="card-body">
          <h5 class="card-title"><i class="fas fa-search"></i> Discover Resources</h5>
          <input v-model="discoverEndpoint" type="text" class="form-control mb-2" placeholder="Endpoint">
          <input v-model="discoverResourcePath" type="text" class="form-control mb-2" placeholder="Resource Path">
          <button class="btn btn-outline-info" @click="discoverResources">Discover</button>
          <pre id="resultBox" class="mt-3">{{ result }}</pre>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      result: '',
      discoverEndpoint: '',
      discoverResourcePath: '',
      actions: [
        {
          title: 'List All Devices',
          icon: 'fas fa-list',
          method: this.listAllDevices
        },
        {
          title: 'Observe Device',
          icon: 'fas fa-eye',
          inputs: [
            { placeholder: 'Endpoint', value: '' },
            { placeholder: 'Resource Path', value: '' }
          ],
          method: this.observeDevice
        },
        {
          title: 'Cancel Observation',
          icon: 'fas fa-ban',
          inputs: [
            { placeholder: 'Endpoint', value: '' },
            { placeholder: 'Resource Path', value: '' }
          ],
          method: this.cancelObservation
        },
        {
          title: 'Reboot Device',
          icon: 'fas fa-sync-alt',
          inputs: [
            { placeholder: 'Endpoint', value: '' }
          ],
          method: this.rebootDevice
        },
        {
          title: 'Reboot Server',
          icon: 'fas fa-server',
          method: this.rebootServer
        },
        {
          title: 'Deregister Device',
          icon: 'fas fa-user-slash',
          inputs: [
            { placeholder: 'Endpoint', value: '' }
          ],
          method: this.deregisterDevice
        },
        {
          title: 'Write Resource',
          icon: 'fas fa-pencil-alt',
          inputs: [
            { placeholder: 'Endpoint', value: '' },
            { placeholder: 'Resource Path', value: '' },
            { placeholder: 'Value', value: '' }
          ],
          method: this.writeResource
        },
        {
          title: 'Create Instance',
          icon: 'fas fa-plus',
          inputs: [
            { placeholder: 'Endpoint', value: '' },
            { placeholder: 'Object ID', value: '' },
            { placeholder: 'Instance ID', value: '' }
          ],
          method: this.createInstance
        }
      ]
    };
  },
  methods: {
    async listAllDevices() {
      try {
        const response = await axios.get('http://localhost:8080/api/devices/list');
        this.result = `Devices: ${JSON.stringify(response.data)}`;
      } catch (error) {
        this.result = `Error: ${error.message}`;
      }
    },
    async observeDevice() {
      const endpoint = this.actions[1].inputs[0].value;
      const resourcePath = this.actions[1].inputs[1].value;
      try {
        const response = await axios.post('/api/observe', { endpoint, resourcePath });
        this.result = `Observation started: ${JSON.stringify(response.data)}`;
      } catch (error) {
        this.result = `Error: ${error.message}`;
      }
    },
    async cancelObservation() {
      const endpoint = this.actions[2].inputs[0].value;
      const resourcePath = this.actions[2].inputs[1].value;
      try {
        const response = await axios.post('/api/cancel-observation', { endpoint, resourcePath });
        this.result = `Observation cancelled: ${JSON.stringify(response.data)}`;
      } catch (error) {
        this.result = `Error: ${error.message}`;
      }
    },
    async rebootDevice() {
      const endpoint = this.actions[3].inputs[0].value;
      try {
        const response = await axios.post('/api/reboot-device', { endpoint });
        this.result = `Reboot command sent: ${JSON.stringify(response.data)}`;
      } catch (error) {
        this.result = `Error: ${error.message}`;
      }
    },
    async rebootServer() {
      try {
        const response = await axios.post('/api/reboot-server');
        this.result = `Server rebooted: ${JSON.stringify(response.data)}`;
      } catch (error) {
        this.result = `Error: ${error.message}`;
      }
    },
    async deregisterDevice() {
      const endpoint = this.actions[5].inputs[0].value;
      try {
        const response = await axios.post('/api/deregister-device', { endpoint });
        this.result = `Deregistration command sent: ${JSON.stringify(response.data)}`;
      } catch (error) {
        this.result = `Error: ${error.message}`;
      }
    },
    async writeResource() {
      const endpoint = this.actions[6].inputs[0].value;
      const resourcePath = this.actions[6].inputs[1].value;
      const value = this.actions[6].inputs[2].value;
      try {
        const response = await axios.post('/api/write-resource', { endpoint, resourcePath, value });
        this.result = `Write command sent: ${JSON.stringify(response.data)}`;
      } catch (error) {
        this.result = `Error: ${error.message}`;
      }
    },
    async createInstance() {
      const endpoint = this.actions[7].inputs[0].value;
      const objectId = this.actions[7].inputs[1].value;
      const instanceId = this.actions[7].inputs[2].value;
      try {
        const response = await axios.post('/api/create-instance', { endpoint, objectId, instanceId });
        this.result = `Instance created: ${JSON.stringify(response.data)}`;
      } catch (error) {
        this.result = `Error: ${error.message}`;
      }
    },
    async discoverResources() {
      try {
        const response = await axios.post('/api/discover-resources', { endpoint: this.discoverEndpoint, resourcePath: this.discoverResourcePath });
        this.result = `Resources discovered: ${JSON.stringify(response.data)}`;
      } catch (error) {
        this.result = `Error: ${error.message}`;
      }
    },
    refreshPage() {
      window.location.reload();
    }
  }
};
</script>

<style scoped>
/* Your CSS styles here if needed */
</style>
