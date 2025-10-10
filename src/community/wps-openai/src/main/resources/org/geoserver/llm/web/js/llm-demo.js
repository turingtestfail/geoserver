
    const { Map, View, layer, source, format, style, Overlay, proj } = ol;
    const { Tile: TileLayer, Vector: VectorLayer } = layer;
    const { OSM, Vector: VectorSource } = source;
    const { GeoJSON, WMSCapabilities } = format;
    const { Stroke, Fill, Circle, Style } = style;
    const { fromLonLat } = proj;

    // --- Config ---
    const WPS_URL = "http://localhost:8080/geoserver/ows?strict=true"; // CHANGE THIS
    const WMS_CAPABILITIES = "http://localhost:8080/geoserver/ows?service=WMS&request=GetCapabilities";
    const WFS_GETFEATUREINFO = "http://localhost:8080/geoserver/ows?service=WFS&version=1.1.0&request=DescribeFeatureType&typeName="
    // --- Session state ---
    let sessionId = "";

    // --- Map setup ---
    const vectorSource = new VectorSource();
    const vectorLayer = new VectorLayer({
      source: vectorSource,
      style: new Style({
        stroke: new Stroke({ width: 2, color: "#1f78b4" }),
        fill: new Fill({ color: "rgba(31,120,180,0.15)" }),
        image: new Circle({
                     radius: 6,
                     fill: new Fill({ color: "#1f78b4" })
                   })
      })
    });

    const map = new Map({
      target: "map",
      layers: [
        new TileLayer({ source: new OSM() }),
        vectorLayer
      ],
      view: new View({
        center: fromLonLat([0, 20]),
        zoom: 2
      })
    });

    const popupContainer = document.getElementById('popup');
const popupContent = document.getElementById('popup-content');
const popupCloser = document.getElementById('popup-closer');

    const overlay = new Overlay({
  element: popupContainer,
  autoPan: {
    animation: {
      duration: 250,
    },
  },
});
map.addOverlay(overlay);

// Handle closing the popup
popupCloser.onclick = function() {
  overlay.setPosition(undefined);
  popupCloser.blur();
  return false;
};

        map.on('click', function (evt) {
        const coordinate = evt.coordinate;
        let clickedFeature = null;

        map.forEachFeatureAtPixel(evt.pixel, function (feature, layer) {
            if (layer === vectorLayer) {
                clickedFeature = feature;
                return true; // Stop iterating after finding the first feature in our layer
            }
        });

        // Now 'clickedFeature' will hold the feature that was clicked (if any)
        if (clickedFeature) {
            // Access and display attributes
            const properties = clickedFeature.getProperties();
            console.log('Clicked Feature Properties:', properties);

            // Example: Displaying attributes in a popup overlay
            const content = document.getElementById('popup-content');
            content.innerHTML = ''; // Clear previous content
            for (const key in properties) {
                if (properties.hasOwnProperty(key) && key !== 'geometry') { // Exclude geometry
                    content.innerHTML += `<b>${key}:</b> ${properties[key]}<br>`;
                }
            }
            overlay.setPosition(coordinate); // Position your popup overlay
        } else {
            // No feature clicked, hide popup or perform other actions
            overlay.setPosition(undefined);
        }
    });

    // --- Chat UI ---
    const chatLog = document.getElementById("chatLog");
    const input = document.getElementById("input");
    const sendBtn = document.getElementById("sendBtn");
    const newSessionBtn = document.getElementById("newSessionBtn");

    let loadingDiv = null;

    function showLoading() {
      if (loadingDiv) return; // already showing
      loadingDiv = document.createElement("div");
      loadingDiv.className = "msg bot loading";
      loadingDiv.textContent = "Thinking";
      chatLog.appendChild(loadingDiv);
      chatLog.scrollTop = chatLog.scrollHeight;
    }

    function hideLoading() {
      if (loadingDiv) {
        loadingDiv.remove();
        loadingDiv = null;
      }
    }


    function appendMessage(text, who = "bot") {
      const div = document.createElement("div");
      div.className = "msg " + who;
      div.textContent = text;
      chatLog.appendChild(div);
      chatLog.scrollTop = chatLog.scrollHeight;
    }

    async function postChat(question) {
      appendMessage(question, "user");
      showLoading();

      // --- Build XML request ---
      const xml = `<?xml version="1.0" encoding="UTF-8"?>
<wps:Execute version="1.0.0" service="WPS"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:wps="http://www.opengis.net/wps/1.0.0"
  xmlns:ows="http://www.opengis.net/ows/1.1"
  xsi:schemaLocation="http://www.opengis.net/wps/1.0.0
  http://schemas.opengis.net/wps/1.0.0/wpsAll.xsd">

  <ows:Identifier>gs:OpenAI</ows:Identifier>
  <wps:DataInputs>
    <wps:Input>
      <ows:Identifier>question</ows:Identifier>
      <wps:Data>
        <wps:LiteralData>${question}</wps:LiteralData>
      </wps:Data>
    </wps:Input>
    <wps:Input>
      <ows:Identifier>session_id</ows:Identifier>
      <wps:Data>
        <wps:LiteralData>${sessionId}</wps:LiteralData>
      </wps:Data>
    </wps:Input>
    <wps:Input>
      <ows:Identifier>return_data</ows:Identifier>
      <wps:Data>
        <wps:LiteralData>true</wps:LiteralData>
      </wps:Data>
    </wps:Input>
  </wps:DataInputs>
  <wps:ResponseForm>
    <wps:RawDataOutput>
      <ows:Identifier>result</ows:Identifier>
    </wps:RawDataOutput>
  </wps:ResponseForm>
</wps:Execute>`;

      try {
        getLayers().then(layers=>console.log(layers));
        getLayerSchema("topp:states").then(attrs => console.log(attrs));

        const resp = await fetch(WPS_URL, {
          method: "POST",
          headers: { "Content-Type": "text/xml" },
          body: xml
        });

        const text = await resp.text();
        hideLoading();
        const parsed = JSON.parse(text);

        appendMessage("WPS responded.", "bot");

        // --- Save session id if returned ---
        if (parsed.sessionId) {
          sessionId = parsed.sessionId;
        }
        if (parsed.cqls && parsed.cqls[0]) {
          appendMessage("ECQL Query: " + parsed.cqls[0].ecql, "bot");

          const layerName = parsed.cqls[0].layerName;
          const div = document.createElement("div");
          div.className = "msg bot";
          div.innerHTML = `
            GeoServer Layer:
            <a href="#" class="layer-link" data-layer="${layerName}" style="color: var(--accent); text-decoration: underline;">
              ${layerName}
            </a>
            <div class="layer-attributes" style="margin-top:6px; display:none;"></div>
          `;
          chatLog.appendChild(div);
          chatLog.scrollTop = chatLog.scrollHeight;

          const link = div.querySelector(".layer-link");
          const attrContainer = div.querySelector(".layer-attributes");
          let loaded = false; // Track if attributes have been loaded yet
          let open = false;   // Track open/closed state

          link.addEventListener("click", async (e) => {
            e.preventDefault();
            open = !open; // Toggle open/close

            if (!open) {
              attrContainer.style.display = "none";
              return;
            }

            attrContainer.style.display = "block";

            if (!loaded) {
              attrContainer.innerHTML = "Loading attributes…";
              try {
                const attrs = await getLayerSchema(layerName);
                if (attrs.length === 0) {
                  attrContainer.innerHTML = "<i>No attributes found.</i>";
                } else {
                  attrContainer.innerHTML =
                    `<b>Attributes for ${layerName}:</b><br>` +
                    attrs.map(a => `• ${a.name} <span style="color:#777">(${a.type})</span>`).join("<br>");
                }
                loaded = true;
              } catch (err) {
                attrContainer.innerHTML = `<span style="color:red">Failed to load attributes: ${err.message}</span>`;
              }
            }
          });
}


        if (parsed.geoJSON&&parsed.geoJSON!="") {
          const geojsonObj = JSON.parse(parsed.geoJSON);
          addGeoJsonToMap(geojsonObj);
          appendMessage("Rendering GeoJSON on map.", "bot");
        } else {
          appendMessage("No GeoJSON found in response.", "bot");
        }
      } catch (err) {
        hideLoading();
        appendMessage("Error: " + err.message, "bot");
      }
    }

    function addGeoJsonToMap(geojsonObj) {
      try {
        const fmt = new GeoJSON({ featureProjection: map.getView().getProjection() });
        const features = fmt.readFeatures(geojsonObj);
        vectorSource.clear();
        vectorSource.addFeatures(features);
        map.getView().fit(vectorSource.getExtent(), { padding: [30, 30, 30, 30], maxZoom: 18 });
      } catch (e) {
        appendMessage("Failed to parse GeoJSON: " + e.message, "bot");
      }
    }

    function getLayers(){
        return fetch(WMS_CAPABILITIES)
          .then(res => res.text())
          .then(text => {
          const parser = new WMSCapabilities();
          const result = parser.read(text);
          return result.Capability.Layer.Layer; // list of layers
        });
    }

    function getLayerSchema(typeName) {
        const url = WFS_GETFEATUREINFO+typeName;

        return fetch(url)
          .then(res => res.text())
          .then(text => {
            const xml = new DOMParser().parseFromString(text, 'text/xml');
            const elements = xml.getElementsByTagName("xsd:element");

            const attributes = [];
            for (let i = 0; i < elements.length; i++) {
              const el = elements[i];
              attributes.push({
                name: el.getAttribute("name"),
                type: el.getAttribute("type")
              });
            }
            return attributes;
      });
}

    // --- Events ---
    sendBtn.addEventListener("click", () => {
      const text = input.value.trim();
      if (!text) return;
      postChat(text);
      input.value = "";
    });

    input.addEventListener("keydown", (e) => {
      if (e.key === "Enter" && !e.shiftKey) {
        e.preventDefault();
        sendBtn.click();
      }
    });

    newSessionBtn.addEventListener("click", () => {
      sessionId = "";
      vectorSource.clear();
      appendMessage("🔄 New session started. Session ID cleared.", "bot");
    });

    // Greeting
    appendMessage("Hello! Ask me something — if GeoServer WPS returns GeoJSON, I'll draw it on the map. Use 'New Session' to reset.");
