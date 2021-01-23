<script lang="ts">
  export let name: string;

  let dark = true; // dark mode
</script>

<style>
  main {
    text-align: center;
    padding: 1em;
    max-width: 240px;
    margin: 0 auto;
  }

  @media (min-width: 640px) {
    main {
      max-width: none;
    }
  }
</style>

<!-- Note: "class:dark" is equivalent (and short for) "class:dark={dark}" or "class:dark={dark === true}" -->
<main
  class="bg-gradient-to-br"
  class:dark
  class:from-blue-100={dark}
  class:to-purple-800={dark}>
  <div class="flex flex-col items-center justify-center min-h-screen space-y-4">
    <div class="flex items-center">
    </div>
  </div>
</main>
