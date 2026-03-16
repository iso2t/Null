# TODOs

This list is a stream of consciousness for what I want to add to the mod.
Currently, the main focus is the Quarry inspired by EU2.

- [x] Dimensional Quarry
- [ ] Arboreal Synthesis Engine (ASE)
- [ ] Florogenesis Engine (FGE)

# Definitions

## Dimensional Quarry
The Dimensional Quarry is a machine that can mine blocks in an alternate dimension. It does this by generating a chunk in
the alternate dimension and then mining the blocks in that chunk. When it finishes, the chunk is regenerated and the
quarry begins to mine the blocks again. This process repeats until the quarry is stopped.

The quarry is powered by RF and has a maximum capacity of 20,000,000 RF. It consumes 20,000 RF per block mined. It mines 
up to one block per tick.
The quarry also has a built-in fluid tank for storing fluids.

## Arboreal Synthesis Engine (ASE)
The Arboreal Synthesis Engine is a multiblock machine designed for automated tree growth and wood production. Rather than 
planting and harvesting trees in the world directly, the ASE simulates arboreal growth within an internal process chamber 
and outputs the resulting materials.

The ASE accepts saplings as its primary growth template and may also require supplemental inputs such as water, biomass, 
fertilizer, or other catalytic materials depending on balance and progression. Once supplied with power and valid inputs, 
the machine begins a growth cycle that produces logs, leaves, saplings, and other tree-related drops based on the selected 
template.

The ASE is powered by RF and is intended to function as a high-throughput forestry machine for mid- to late-game resource 
automation. It should feel more industrial than a simple farm, providing a technical solution for renewable wood 
generation at scale.

Potential features include:
- Tree-type-dependent output tables
- Optional secondary outputs such as sticks, apples, or other modded drops
- Upgrade support for speed, energy efficiency, or output yield
- Internal storage for items and fluids
- Visual or animated multiblock activity while processing

## Florogenesis Engine (FGE)
The Florogenesis Engine is a multiblock machine designed for accelerated plant and crop production. It functions as a 
synthetic cultivation system, using energy and supplied inputs to grow and reproduce plant matter without requiring 
traditional farmland layouts.

The FGE is intended to handle general plant growth, including crops, flowers, seeds, and other botanical materials. 
Depending on implementation, it may accept seeds, crops, or plant blocks as growth templates and generate matching 
outputs through an internal processing cycle.

The machine is powered by RF and serves as a technical alternative to standard farming, enabling compact and scalable 
plant production for automated bases. It should complement the Arboreal Synthesis Engine by covering non-tree plant life 
while maintaining a similar industrial and synthetic design philosophy.

Potential features include:
- Support for multiple plant categories such as crops, flowers, and decorative plants
- Template-based output generation
- Optional fluid requirements such as water or nutrient solution
- Upgrade support for speed, efficiency, or yield
- Internal inventory and fluid storage
- Distinct processing visuals to separate it from the ASE